package com.github.dustinbarnes.tic_tac_toe;

import com.github.dustinbarnes.tic_tac_toe.model.Game;
import com.github.dustinbarnes.tic_tac_toe.model.Player;
import com.github.dustinbarnes.tic_tac_toe.model.Move;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    // In-memory store for demonstration
    private final Map<String, Game> games = new HashMap<>();

    // 1. Create a new game
    @PostMapping
    public ResponseEntity<String> createGame() {
        Game game = new Game();
        games.put(game.getId(), game);
        logger.info("Created new game with id: {}", game.getId());
        return ResponseEntity.ok(game.getId());
    }

    // 2. Get the current state of a specific game
    @GetMapping("/{gameId}")
    public ResponseEntity<Object> getGame(@PathVariable String gameId) {
        logger.info("Fetching game state for id: {}", gameId);
        Game game = games.get(gameId);
        if (game == null) {
            logger.warn("Game not found: {}", gameId);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }

    // 3. List all games
    @GetMapping
    public ResponseEntity<Set<String>> listGames() {
        logger.info("Listing all game ids");
        return ResponseEntity.ok(games.keySet());
    }

    // 4. Join an existing game
    @PostMapping("/{gameId}/join")
    public ResponseEntity<Player> joinGame(@PathVariable String gameId, @RequestBody Player player) {
        logger.info("Player {} (id: {}) attempting to join game {}", player.getName(), player.getId(), gameId);
        Game game = games.get(gameId);
        if (game == null) {
            logger.warn("Game not found for join: {}", gameId);
            return ResponseEntity.notFound().build();
        }
        if (game.getPlayerX() == null) {
            player.setRole(Player.Role.X);
            game.setPlayerX(player);
            logger.info("Player {} joined as X in game {}", player.getName(), gameId);
            return ResponseEntity.ok(player);
        } else if (game.getPlayerO() == null) {
            player.setRole(Player.Role.O);
            game.setPlayerO(player);
            logger.info("Player {} joined as O in game {}", player.getName(), gameId);
            return ResponseEntity.ok(player);
        } else {
            logger.warn("Game {} is full, join rejected for player {}", gameId, player.getName());
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 5. Make a move
    @PostMapping("/{gameId}/move")
    public ResponseEntity<Game> makeMove(@PathVariable String gameId, @RequestBody Move move) {
        logger.info("Move requested in game {} by player {} at ({}, {})", gameId, move.getPlayer() != null ? move.getPlayer().getName() : "?", move.getRow(), move.getCol());
        Game game = games.get(gameId);
        if (game == null) {
            logger.warn("Game not found for move: {}", gameId);
            return ResponseEntity.notFound().build();
        }
        boolean success = game.addMove(move);
        if (!success) {
            logger.warn("Invalid move in game {} by player {} at ({}, {})", gameId, move.getPlayer() != null ? move.getPlayer().getName() : "?", move.getRow(), move.getCol());
            return ResponseEntity.badRequest().body(null);
        }
        logger.info("Move successful in game {} by player {} at ({}, {})", gameId, move.getPlayer() != null ? move.getPlayer().getName() : "?", move.getRow(), move.getCol());
        return ResponseEntity.ok(game);
    }

    // 6. Get move history
    @GetMapping("/{gameId}/moves")
    public ResponseEntity<List<Move>> getMoves(@PathVariable String gameId) {
        logger.info("Fetching move history for game {}", gameId);
        Game game = games.get(gameId);
        if (game == null) {
            logger.warn("Game not found for move history: {}", gameId);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game.getMoves());
    }

    // 7. Get game status
    @GetMapping("/{gameId}/status")
    public ResponseEntity<String> getStatus(@PathVariable String gameId) {
        logger.info("Fetching status for game {}", gameId);
        Game game = games.get(gameId);
        if (game == null) {
            logger.warn("Game not found for status: {}", gameId);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game.getStatus());
    }

    // 8. Get player stats by id
    @GetMapping("/player/{playerId}/stats")
    public ResponseEntity<Map<String, Integer>> getPlayerStats(@PathVariable String playerId) {
        // Search all games for this player and aggregate stats
        int wins = 0, losses = 0, draws = 0;
        for (Game game : games.values()) {
            Player x = game.getPlayerX();
            Player o = game.getPlayerO();
            if (x != null && playerId.equals(x.getId())) {
                wins += x.getWins();
                losses += x.getLosses();
                draws += x.getDraws();
            }
            if (o != null && playerId.equals(o.getId())) {
                wins += o.getWins();
                losses += o.getLosses();
                draws += o.getDraws();
            }
        }
        Map<String, Integer> stats = new HashMap<>();
        stats.put("wins", wins);
        stats.put("losses", losses);
        stats.put("draws", draws);
        return ResponseEntity.ok(stats);
    }
}
