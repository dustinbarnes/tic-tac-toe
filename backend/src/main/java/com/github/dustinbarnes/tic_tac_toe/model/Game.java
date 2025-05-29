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
    private String nextTurn; // "X" or "O"
    private List<Move> moves;
    private Player winner; // caches the winner, read-only

    public Game() {
        this.id = UUID.randomUUID().toString();
        this.board = new Player[3][3];
        this.status = "IN_PROGRESS";
        this.nextTurn = "X";
        this.moves = new ArrayList<>();
    }

    public String getId() { return id; }
    public Player getPlayerX() { return playerX; }
    public void setPlayerX(Player playerX) { this.playerX = playerX; }
    public Player getPlayerO() { return playerO; }
    public void setPlayerO(Player playerO) { this.playerO = playerO; }
    public Player[][] getBoard() { return board; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNextTurn() { return nextTurn; }
    public void setNextTurn(String nextTurn) { this.nextTurn = nextTurn; }
    public List<Move> getMoves() { return moves; }
    public void setMoves(List<Move> moves) { this.moves = moves; }

    public boolean isWin() {
        return getWinnerPlayer() != null;
    }

    public boolean isDraw() {
        if (getWinnerPlayer() != null) return false;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == null) return false;
            }
        }
        return true;
    }

    public Player getWinnerPlayer() {
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
}
