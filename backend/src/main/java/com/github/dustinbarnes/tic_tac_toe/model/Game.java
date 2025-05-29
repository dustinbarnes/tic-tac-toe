package com.github.dustinbarnes.tic_tac_toe.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Game {
    private static final Logger logger = LoggerFactory.getLogger(Game.class);
    private String id;
    private Player playerX;
    private Player playerO;
    private Player[][] board;
    private String status; // e.g., IN_PROGRESS, X_WON, O_WON, DRAW
    private List<Move> moves;
    private Player winner; // caches the winner, read-only

    public Game() {
        this.id = UUID.randomUUID().toString();
        this.board = new Player[3][3];
        this.status = "WAITING"; // New status for waiting for players
        this.moves = new ArrayList<>();
    }

    public String getId() { return id; }
    public Player getPlayerX() { return playerX; }
    public void setPlayerX(Player playerX) {
        this.playerX = playerX;
        updateStatusIfReady();
    }
    public Player getPlayerO() { return playerO; }
    public void setPlayerO(Player playerO) {
        this.playerO = playerO;
        updateStatusIfReady();
    }
    private void updateStatusIfReady() {
        if (playerX != null && playerO != null && "WAITING".equals(this.status)) {
            this.status = "IN_PROGRESS";
        }
    }
    public Player[][] getBoard() { return board; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Move> getMoves() { return moves; }
    
    public boolean addMove(Move move) {
        int row = move.getRow();
        int col = move.getCol();
        Player player = move.getPlayer();

        // Only allow move if game is started, cell is empty, game is not over, and player is valid
        if (!"IN_PROGRESS".equals(this.status)) {
            logger.warn("Move rejected: Game is not in progress. GameId={}, Player={}", id, player);
            return false;
        }
        if (!player.equals(playerX) && !player.equals(playerO)) {
            logger.warn("Move rejected: Player is not part of this game. GameId={}, Player={}", id, player);
            return false;
        }
        // Enforce turn order: only the expected player can move
        Player expectedPlayer = getNextTurn();
        if (!player.equals(expectedPlayer)) {
            logger.warn("Move rejected: Not this player's turn. GameId={}, Player={}, ExpectedPlayer={}", id, player, expectedPlayer);
            return false;
        }
        if (row < 0 || row > 2 || col < 0 || col > 2) {
            logger.warn("Move rejected: Row or column out of bounds. GameId={}, Player={}, Row={}, Col={}", id, player, row, col);
            return false;
        }
        if (board[row][col] != null) {
            logger.warn("Move rejected: Cell is already occupied. GameId={}, Player={}, Row={}, Col={}", id, player, row, col);
            return false;
        }
        if (isWin()) {
            logger.warn("Move rejected: Game already has a winner. GameId={}, Player={}", id, player);
            return false;
        }
        if (isDraw()) {
            logger.warn("Move rejected: Game is a draw. GameId={}, Player={}", id, player);
            return false;
        }
        
        board[row][col] = player;
        moves.add(move);
        logger.info("Move accepted: GameId={}, Player={}, Row={}, Col={}", id, player, row, col);
        // Check for winner after move
        Player winner = getWinner();
        if (winner != null) {
            if (winner.getRole() == Player.Role.X) {
                this.status = "X_WON";
                if (playerX != null) playerX.setWins(playerX.getWins() + 1);
                if (playerO != null) playerO.setLosses(playerO.getLosses() + 1);
            } else if (winner.getRole() == Player.Role.O) {
                this.status = "O_WON";
                if (playerO != null) playerO.setWins(playerO.getWins() + 1);
                if (playerX != null) playerX.setLosses(playerX.getLosses() + 1);
            } else {
                this.status = "WINNER";
            }
            logger.info("Game won: GameId={}, Winner={}, Status={}", id, winner, this.status);
        } else if (isDraw()) {
            this.status = "DRAW";
            if (playerX != null) playerX.setDraws(playerX.getDraws() + 1);
            if (playerO != null) playerO.setDraws(playerO.getDraws() + 1);
            logger.info("Game draw: GameId={}", id);
        }
        return true;
    }

    public boolean isWin() {
        return getWinner() != null;
    }

    public boolean isDraw() {
        if (getWinner() != null) return false;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == null) return false;
            }
        }
        return true;
    }

    public Player getWinner() {
        if (winner != null) return winner;
        // Check rows and columns
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != null && board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2])) {
                winner = board[i][0];
                return winner;
            }
            if (board[0][i] != null && board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i])) {
                winner = board[0][i];
                return winner;
            }
        }
        // Check diagonals
        if (board[0][0] != null && board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2])) {
            winner = board[0][0];
            return winner;
        }
        if (board[0][2] != null && board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0])) {
            winner = board[0][2];
            return winner;
        }
        return null;
    }

    public Player getNextTurn() {
        Player.Role nextRole;
        if (moves.isEmpty()) nextRole = Player.Role.X;
        else {
            int xCount = 0;
            int oCount = 0;
            for (Move move : moves) {
                if (move.getPlayer() != null && move.getPlayer().getRole() == Player.Role.X) xCount++;
                if (move.getPlayer() != null && move.getPlayer().getRole() == Player.Role.O) oCount++;
            }
            nextRole = (xCount <= oCount) ? Player.Role.X : Player.Role.O;
        }
        if (nextRole == Player.Role.X) return playerX;
        if (nextRole == Player.Role.O) return playerO;
        return null;
    }
}
