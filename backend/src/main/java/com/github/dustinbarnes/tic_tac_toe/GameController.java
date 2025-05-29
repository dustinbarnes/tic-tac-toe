package com.github.dustinbarnes.tic_tac_toe;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/games")
public class GameController {

    // In-memory store for demonstration
    private final Map<String, Game> games = new HashMap<>();

    // 1. Create a new game
    @PostMapping
    public ResponseEntity<String> createGame() {
        Game game = new Game();
        games.put(game.getId(), game);
        return ResponseEntity.ok(game.getId());
    }

    // 2. Get the current state of a specific game
    @GetMapping("/{gameId}")
    public ResponseEntity<Object> getGame(@PathVariable String gameId) {
        Game game = games.get(gameId);
        if (game == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(game);
    }

    // 3. List all games
    @GetMapping
    public ResponseEntity<Set<String>> listGames() {
        return ResponseEntity.ok(games.keySet());
    }

    // 4. Join an existing game
    @PostMapping("/{gameId}/join")
    public ResponseEntity<String> joinGame(@PathVariable String gameId, @RequestParam String player) {
        // TODO: Add player to game
        if (!games.containsKey(gameId)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok("Player joined");
    }

    // 5. Make a move
    @PostMapping("/{gameId}/move")
    public ResponseEntity<String> makeMove(@PathVariable String gameId, @RequestBody Map<String, Object> move) {
        // TODO: Apply move to game
        if (!games.containsKey(gameId)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok("Move accepted");
    }

    // 6. Get move history
    @GetMapping("/{gameId}/moves")
    public ResponseEntity<List<Object>> getMoves(@PathVariable String gameId) {
        // TODO: Return move history
        if (!games.containsKey(gameId)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Collections.emptyList());
    }

    // 7. Get game status
    @GetMapping("/{gameId}/status")
    public ResponseEntity<String> getStatus(@PathVariable String gameId) {
        // TODO: Return game status
        if (!games.containsKey(gameId)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok("IN_PROGRESS");
    }
}
