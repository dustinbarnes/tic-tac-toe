package com.github.dustinbarnes.tic_tac_toe;

import com.github.dustinbarnes.tic_tac_toe.model.Game;
import com.github.dustinbarnes.tic_tac_toe.model.Player;
import com.github.dustinbarnes.tic_tac_toe.model.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

@WebMvcTest(GameController.class)
public class GameControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Player playerX;
    private Player playerO;

    @BeforeEach
    void setUp() {
        playerX = new Player("1", "Alice");
        playerO = new Player("2", "Bob");
    }

    @Test
    void createGame_returnsGameId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.isEmptyOrNullString())));
    }

    @Test
    void joinGame_assignsRoles() throws Exception {
        // Create game
        String gameId = mockMvc.perform(MockMvcRequestBuilders.post("/api/games"))
                .andReturn().getResponse().getContentAsString();
        // Join as X
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/" + gameId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(playerX)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value("X"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Alice"));
        // Join as O
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/" + gameId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(playerO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value("O"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Bob"));
        // Third join fails
        Player player3 = new Player();
        player3.setName("Charlie");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/" + gameId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(player3)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void makeMove_andGetGameState() throws Exception {
        // Create game and join
        String gameId = mockMvc.perform(MockMvcRequestBuilders.post("/api/games"))
                .andReturn().getResponse().getContentAsString();
        // Join as X and capture returned Player (with id)
        String playerXJson = mockMvc.perform(MockMvcRequestBuilders.post("/api/games/" + gameId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(playerX)))
                .andReturn().getResponse().getContentAsString();
        Player joinedX = objectMapper.readValue(playerXJson, Player.class);
        // Join as O and capture returned Player (with id)
        String playerOJson = mockMvc.perform(MockMvcRequestBuilders.post("/api/games/" + gameId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(playerO)))
                .andReturn().getResponse().getContentAsString();
        Player joinedO = objectMapper.readValue(playerOJson, Player.class);
        // Make move as X (use returned Player with id)
        Move move = new Move();
        move.setPlayer(joinedX);
        move.setRow(0);
        move.setCol(0);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/" + gameId + "/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(move)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.board[0][0].role").value("X"));
        // Get game state
        mockMvc.perform(MockMvcRequestBuilders.get("/api/games/" + gameId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.board[0][0].role").value("X"));
    }

    @Test
    void getMoves_returnsMoveHistory() throws Exception {
        // Create game and join
        String gameId = mockMvc.perform(MockMvcRequestBuilders.post("/api/games"))
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/" + gameId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(playerX)));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/" + gameId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(playerO)));
        // Make move as X
        Move move = new Move();
        move.setPlayer(playerX);
        move.setRow(0);
        move.setCol(0);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/" + gameId + "/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(move)));
        // Get moves
        mockMvc.perform(MockMvcRequestBuilders.get("/api/games/" + gameId + "/moves"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].row").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].col").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].player.name").value("Alice"));
    }

    @Test
    void getStatus_returnsGameStatus() throws Exception {
        // Create game and join
        String gameId = mockMvc.perform(MockMvcRequestBuilders.post("/api/games"))
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/" + gameId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(playerX)));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/" + gameId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(playerO)));
        // Status should be IN_PROGRESS
        mockMvc.perform(MockMvcRequestBuilders.get("/api/games/" + gameId + "/status"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("IN_PROGRESS")));
    }

    @Test
    void getGame_notFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/games/doesnotexist"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void makeMove_invalidMove_returnsBadRequest() throws Exception {
        // Create game and join
        String gameId = mockMvc.perform(MockMvcRequestBuilders.post("/api/games"))
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/" + gameId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(playerX)));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/" + gameId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(playerO)));
        // Make move as X
        Move move = new Move();
        move.setPlayer(playerX);
        move.setRow(0);
        move.setCol(0);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/" + gameId + "/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(move)));
        // Try to make the same move again (invalid)
        mockMvc.perform(MockMvcRequestBuilders.post("/api/games/" + gameId + "/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(move)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
