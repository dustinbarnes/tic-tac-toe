package com.github.dustinbarnes.tic_tac_toe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {
    private String id;
    private Player playerX;
    private Player playerO;
    private String[][] board;
    private String status; // e.g., IN_PROGRESS, X_WON, O_WON, DRAW
    private String nextTurn; // "X" or "O"
    private List<Move> moves;
    private Player winner; // caches the winner, read-only

    public Game() {
        this.id = UUID.randomUUID().toString();
        this.board = new String[3][3];
        this.status = "IN_PROGRESS";
        this.nextTurn = "X";
        this.moves = new ArrayList<>();
    }

    public String getId() { return id; }
    public Player getPlayerX() { return playerX; }
    public void setPlayerX(Player playerX) { this.playerX = playerX; }
    public Player getPlayerO() { return playerO; }
    public void setPlayerO(Player playerO) { this.playerO = playerO; }
    public String[][] getBoard() { return board; }
    public void setBoard(String[][] board) { this.board = board; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNextTurn() { return nextTurn; }
    public void setNextTurn(String nextTurn) { this.nextTurn = nextTurn; }
    public List<Move> getMoves() { return moves; }
    public void setMoves(List<Move> moves) { this.moves = moves; }

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

    public String getWinner() {
        // Check rows and columns
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != null && board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2])) {
                return board[i][0];
            }
            if (board[0][i] != null && board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i])) {
                return board[0][i];
            }
        }
        // Check diagonals
        if (board[0][0] != null && board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2])) {
            return board[0][0];
        }
        if (board[0][2] != null && board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0])) {
            return board[0][2];
        }
        return null;
    }

    public Player getWinnerPlayer() {
        if (winner != null) return winner;
        String winnerSymbol = getWinner();
        if (winnerSymbol == null) return null;
        if (winnerSymbol.equals("X")) {
            winner = playerX;
        } else if (winnerSymbol.equals("O")) {
            winner = playerO;
        }
        return winner;
    }
}

class Move {
    private int row;
    private int col;
    private String play; // "X" or "O"

    public Move() {}
    public Move(int row, int col, String play) {
        this.row = row;
        this.col = col;
        this.play = play;
    }
    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }
    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }
    public String getPlay() { return play; }
    public void setPlay(String play) { this.play = play; }
}
