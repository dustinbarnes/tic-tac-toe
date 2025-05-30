export interface Player {
  id: string;
  name: string;
  role: 'X' | 'O';
}

export interface Move {
  row: number;
  col: number;
  player: Player;
}

export interface Game {
  id: string;
  board: (Player | null)[][];
  status: string;
  playerX: Player | null;
  playerO: Player | null;
  moves: Move[];
}

export interface PlayerStats {
  wins: number;
  losses: number;
  draws: number;
}
