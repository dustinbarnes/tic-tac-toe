package com.github.dustinbarnes.tic_tac_toe.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {
    private Player playerX;
    private Player playerO;
    private Game game;

    @BeforeEach
    public void setUp() {
        playerX = new Player("1", "Alice");
        playerX.setRole(Player.Role.X);
        playerO = new Player("2", "Bob");
        playerO.setRole(Player.Role.O);
        game = new Game();
        game.setPlayerX(playerX);
        game.setPlayerO(playerO);
    }

    @Test
    public void testInitialStatusIsWaiting() {
        Game newGame = new Game();
        assertEquals("WAITING", newGame.getStatus());
    }

    @Test
    public void testStatusTransitionsToInProgress() {
        assertEquals("IN_PROGRESS", game.getStatus());
    }

    @Test
    public void testAddMoveValid() {
        Move move = new Move(0, 0, playerX);
        assertTrue(game.addMove(move));
        assertEquals(playerX, game.getBoard()[0][0]);
        assertEquals(1, game.getMoves().size());
    }

    @Test
    public void testAddMoveInvalidCellOccupied() {
        Move move1 = new Move(0, 0, playerX);
        Move move2 = new Move(0, 0, playerO);
        assertTrue(game.addMove(move1));
        assertFalse(game.addMove(move2));
    }

    @Test
    public void testAddMoveInvalidPlayer() {
        Player other = new Player("3", "Charlie");
        other.setRole(Player.Role.X);
        Move move = new Move(0, 1, other);
        assertFalse(game.addMove(move));
    }

    @Test
    public void testWinRow() {
        game.addMove(new Move(0, 0, playerX));
        game.addMove(new Move(1, 0, playerO));
        game.addMove(new Move(0, 1, playerX));
        game.addMove(new Move(1, 1, playerO));
        game.addMove(new Move(0, 2, playerX));
        assertTrue(game.isWin());
        assertEquals(playerX, game.getWinner());
        assertEquals("WINNER", game.getStatus());
    }

    @Test
    public void testWinColumn() {
        game.addMove(new Move(0, 0, playerX));
        game.addMove(new Move(0, 1, playerO));
        game.addMove(new Move(1, 0, playerX));
        game.addMove(new Move(1, 1, playerO));
        game.addMove(new Move(2, 0, playerX));
        assertTrue(game.isWin());
        assertEquals(playerX, game.getWinner());
        assertEquals("WINNER", game.getStatus());
    }

    @Test
    public void testWinDiagonal() {
        game.addMove(new Move(0, 0, playerX));
        game.addMove(new Move(0, 1, playerO));
        game.addMove(new Move(1, 1, playerX));
        game.addMove(new Move(0, 2, playerO));
        game.addMove(new Move(2, 2, playerX));
        assertTrue(game.isWin());
        assertEquals(playerX, game.getWinner());
        assertEquals("WINNER", game.getStatus());
    }

    @Test
    public void testDraw() {
        // X O X
        // X O O
        // O X X
        game.addMove(new Move(0, 0, playerX));
        game.addMove(new Move(0, 1, playerO));
        game.addMove(new Move(0, 2, playerX));
        game.addMove(new Move(1, 1, playerO));
        game.addMove(new Move(1, 0, playerX));
        game.addMove(new Move(1, 2, playerO));
        game.addMove(new Move(2, 1, playerX));
        game.addMove(new Move(2, 0, playerO));
        game.addMove(new Move(2, 2, playerX));
        assertTrue(game.isDraw());
        assertEquals("DRAW", game.getStatus());
    }

    @Test
    public void testGetNextTurn() {
        assertEquals(playerX, game.getNextTurn());
        game.addMove(new Move(0, 0, playerX));
        assertEquals(playerO, game.getNextTurn());
        game.addMove(new Move(1, 1, playerO));
        assertEquals(playerX, game.getNextTurn());
    }

    @Test
    public void testAddMoveNotStarted() {
        Game notStarted = new Game();
        Player px = new Player("1", "Alice");
        px.setRole(Player.Role.X);
        notStarted.setPlayerX(px);
        // Only one player, so status is still WAITING
        Move move = new Move(0, 0, px);
        assertFalse(notStarted.addMove(move));
    }
}
