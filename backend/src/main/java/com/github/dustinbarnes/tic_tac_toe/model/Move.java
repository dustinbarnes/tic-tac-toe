package com.github.dustinbarnes.tic_tac_toe.model;

public class Move {
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
