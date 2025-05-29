// Uncomment this line to use CSS modules
// import styles from './app.module.css';
import { useEffect, useState } from 'react';
import { v4 as uuidv4 } from 'uuid';

// Types
interface Player {
  id: string;
  name: string;
  role: 'X' | 'O';
}
interface Move {
  row: number;
  col: number;
  player: Player;
}
interface Game {
  id: string;
  board: (Player | null)[][];
  status: string;
  playerX: Player | null;
  playerO: Player | null;
  moves: Move[];
}
// Add a new type for player stats
interface PlayerStats {
  wins: number;
  losses: number;
  draws: number;
}

const API = 'http://localhost:8080/api/games';

function emptyBoard() {
  return Array(3).fill(null).map(() => Array(3).fill(null));
}

export default function App() {
  const [user, setUserState] = useState<{ id: string; name: string } | null>(null);
  const [username, setUsername] = useState('');
  const [game, setGame] = useState<Game | null>(null);
  const [player, setPlayer] = useState<Player | null>(null);
  const [message, setMessage] = useState('');
  const [joining, setJoining] = useState(false);

  // List of available games (with player name)
  const [games, setGames] = useState<{ id: string; playerName: string }[]>([]);
  const [loadingGames, setLoadingGames] = useState(false);
  // List of recently finished games
  const [finishedGames, setFinishedGames] = useState<{ id: string; winner: string | null; status: string }[]>([]);
  const [loadingFinished, setLoadingFinished] = useState(false);
  // Player stats state
  const [playerStats, setPlayerStats] = useState<PlayerStats>({ wins: 0, losses: 0, draws: 0 });

  // Create a new game
  async function createGame() {
    const res = await fetch(API, { method: 'POST' });
    const id = await res.text();
    // Immediately join the game as the current user
    const joinRes = await fetch(`${API}/${id}/join`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id: user!.id, name: user!.name })
    });
    let joinedPlayer = null;
    if (joinRes.ok) {
      joinedPlayer = await joinRes.json();
    }
    setGame({ id, board: emptyBoard(), status: 'WAITING', playerX: null, playerO: null, moves: [] });
    setPlayer(joinedPlayer);
    setMessage('Game created. Share the ID to join: ' + id);
  }

  // Join a game
  async function joinGame(gameId: string, name: string) {
    setJoining(true);
    const res = await fetch(`${API}/${gameId}/join`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id: user!.id, name })
    });
    if (res.ok) {
      const p = await res.json();
      setPlayer(p);
      setMessage(`Joined as ${p.role}`);
      fetchGame(gameId);
    } else {
      setMessage('Failed to join game.');
    }
    setJoining(false);
  }

  // Fetch game state
  async function fetchGame(gameId: string) {
    const res = await fetch(`${API}/${gameId}`);
    if (res.ok) {
      const g = await res.json();
      setGame(g);
    } else {
      // If the game is not found or another error, return to game list
      setGame(null);
      setPlayer(null);
      setMessage('Game not found or no longer available.');
    }
  }

  // Make a move
  async function makeMove(row: number, col: number) {
    if (!game || !player) return;
    const res = await fetch(`${API}/${game.id}/move`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ row, col, player })
    });
    if (res.ok) {
      fetchGame(game.id);
    } else {
      setMessage('Invalid move.');
    }
  }

  // Poll for game updates if in progress
  useEffect(() => {
    if (!game || !game.id) return;
    const interval = setInterval(() => fetchGame(game.id), 2000);
    return () => clearInterval(interval);
  }, [game?.id]);

  // Fetch available games
  async function fetchGames() {
    setLoadingGames(true);
    try {
      const res = await fetch(API);
      if (res.ok) {
        const ids = await res.json();
        // Fetch each game's status and filter by WAITING, include player name
        const filtered: { id: string; playerName: string }[] = [];
        const finished: { id: string; winner: string | null; status: string }[] = [];
        await Promise.all(
          ids.map(async (gid: string) => {
            const gres = await fetch(`${API}/${gid}`);
            if (gres.ok) {
              const g = await gres.json();
              if (g.status === 'WAITING' && g.playerX && g.playerX.name) {
                filtered.push({ id: gid, playerName: g.playerX.name });
              } else if (g.status === 'DRAW' || g.status === 'WINNER' || g.status === 'X_WON' || g.status === 'O_WON') {
                let winner = null;
                if (g.status === 'WINNER' && g.winner && g.winner.name) winner = g.winner.name;
                if (g.status === 'X_WON' && g.playerX && g.playerX.name) winner = g.playerX.name;
                if (g.status === 'O_WON' && g.playerO && g.playerO.name) winner = g.playerO.name;
                finished.push({ id: gid, winner, status: g.status });
              }
            }
          })
        );
        setGames(filtered);
        // Sort finished games by most recent (highest id, assuming UUIDs are sortable; otherwise, just reverse)
        setFinishedGames(finished.reverse());
      }
    } finally {
      setLoadingGames(false);
      setLoadingFinished(false);
    }
  }

  // Fetch player stats from backend
  async function fetchPlayerStats(playerId: string) {
    const res = await fetch(`${API}/player/${playerId}/stats`);
    if (res.ok) {
      const stats = await res.json();
      setPlayerStats(stats);
    }
  }

  // Fetch player stats when user or finishedGames changes
  useEffect(() => {
    if (user) fetchPlayerStats(user.id);
  }, [user, finishedGames]);

  // Fetch games after user is created
  useEffect(() => {
    if (user && !game) fetchGames();
  }, [user, game]);

  // Poll for available games if on the game list screen
  useEffect(() => {
    if (user && !game) {
      fetchGames();
      const interval = setInterval(fetchGames, 2000);
      return () => clearInterval(interval);
    }
  }, [user, game]);

  // Persist user in localStorage
  useEffect(() => {
    const stored = localStorage.getItem('ttt-user');
    if (stored) {
      setUserState(JSON.parse(stored));
    }
  }, []);

  function setUser(user: { id: string; name: string }) {
    setUserState(user);
    localStorage.setItem('ttt-user', JSON.stringify(user));
  }

  // Show user creation form if not set
  if (!user) {
    return (
      <div style={{ maxWidth: 400, margin: '2rem auto', textAlign: 'center' }}>
        <h1>Tic-Tac-Toe</h1>
        <form
          onSubmit={e => {
            e.preventDefault();
            if (username.trim()) {
              setUser({ id: uuidv4(), name: username.trim() });
            }
          }}
        >
          <input
            placeholder="Enter your username"
            value={username}
            onChange={e => setUsername(e.target.value)}
            autoFocus
          />
          <button type="submit">Continue</button>
        </form>
      </div>
    );
  }

  // Show game list if user exists and no game is selected
  if (user && !game) {
    return (
      <div style={{ maxWidth: 400, margin: '2rem auto', textAlign: 'center' }}>
        <h1>Welcome, {user.name}!</h1>
        {/* Player stats box */}
        <div style={{ border: '1px solid #aaa', borderRadius: 8, padding: 12, marginBottom: 16, background: '#f9f9f9' }}>
          <b>Your Record</b>
          <div>Wins: {playerStats.wins}</div>
          <div>Losses: {playerStats.losses}</div>
          <div>Draws: {playerStats.draws}</div>
        </div>
        <button onClick={createGame}>Create New Game</button>
        <hr />
        <h2>Join a Game</h2>
        <button onClick={fetchGames} disabled={loadingGames} style={{ marginBottom: 8 }}>
          {loadingGames ? 'Loading...' : 'Refresh List'}
        </button>
        <ul style={{ listStyle: 'none', padding: 0 }}>
          {games.length === 0 && <li>No games available. Create one!</li>}
          {games.map((g) => (
            <li key={g.id} style={{ margin: '0.5rem 0' }}>
              <button onClick={() => joinGame(g.id, user.name)} style={{ width: '100%' }}>
                Join {g.playerName}'s Game ({g.id})
              </button>
            </li>
          ))}
        </ul>
        <hr />
        <h2>Recently Finished Games</h2>
        <button onClick={fetchGames} disabled={loadingFinished} style={{ marginBottom: 8 }}>
          {loadingFinished ? 'Loading...' : 'Refresh List'}
        </button>
        <ul style={{ listStyle: 'none', padding: 0 }}>
          {finishedGames.length === 0 && <li>No finished games yet.</li>}
          {finishedGames.map((g) => (
            <li key={g.id} style={{ margin: '0.5rem 0' }}>
              <button onClick={() => fetchGame(g.id)} style={{ width: '100%' }}>
                {g.status === 'DRAW' ? `Draw (${g.id})` : `Winner: ${g.winner || 'Unknown'} (${g.id})`}
              </button>
            </li>
          ))}
        </ul>
        <button onClick={() => { setUserState(null); localStorage.removeItem('ttt-user'); setUsername(''); }}>Log out</button>
        <p>{message}</p>
      </div>
    );
  }

  // UI
  if (game) {
    return (
      <div style={{ maxWidth: 400, margin: '2rem auto', textAlign: 'center' }}>
        <h1>Tic-Tac-Toe</h1>
        <p>Game ID: {game.id}</p>
        <p>Status: {game.status}</p>
        <Board board={game.board} onCellClick={makeMove} canMove={!!player && game.status === 'IN_PROGRESS'} />
        <div style={{ margin: '1rem 0' }}>
          <b>Players:</b>
          <ul style={{ listStyle: 'none', padding: 0, margin: '0.5rem 0' }}>
            <li>{game.playerX ? `X: ${game.playerX.name}` : 'X: (waiting...)'}</li>
            <li>{game.playerO ? `O: ${game.playerO.name}` : 'O: (waiting...)'}</li>
          </ul>
          {player ? (
            <span>You are {player.name} ({player.role})</span>
          ) : null}
        </div>
        <button onClick={() => fetchGame(game.id)}>Refresh</button>
        <button onClick={() => { setGame(null); setPlayer(null); setMessage(''); }}>Back</button>
        <p>{message}</p>
      </div>
    );
  }

  return null;
}

function Board({ board, onCellClick, canMove }: { board: (Player | null)[][], onCellClick: (row: number, col: number) => void, canMove: boolean }) {
  return (
    <table style={{ margin: '1rem auto', borderCollapse: 'collapse' }}>
      <tbody>
        {board.map((row, i) => (
          <tr key={i}>
            {row.map((cell, j) => (
              <td
                key={j}
                style={{ width: 60, height: 60, border: '1px solid #333', fontSize: 32, textAlign: 'center', cursor: canMove && !cell ? 'pointer' : 'default', background: cell ? '#f0f0f0' : '#fff' }}
                onClick={() => canMove && !cell && onCellClick(i, j)}
              >
                {cell ? cell.role : ''}
              </td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  );
}
