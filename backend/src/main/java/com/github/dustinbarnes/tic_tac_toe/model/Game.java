package com.github.dustinbarnes.tic_tac_toe.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {
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
        if (!"IN_PROGRESS".equals(this.status) || (player != playerX && player != playerO) || row < 0 || row > 2 || col < 0 || col > 2 || board[row][col] != null || isWin() || isDraw()) {
            return false;
        }
        
        board[row][col] = player;
        moves.add(move);
        // Check for winner after move
        if (getWinner() != null) {
            this.status = "WINNER";
        } else if (isDraw()) {
            this.status = "DRAW";
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
            if (board[i][0] != null && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                winner = board[i][0];
                return winner;
            }
            if (board[0][i] != null && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                winner = board[0][i];
                return winner;
            }
        }
        // Check diagonals
        if (board[0][0] != null && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            winner = board[0][0];
            return winner;
        }
        if (board[0][2] != null && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
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
