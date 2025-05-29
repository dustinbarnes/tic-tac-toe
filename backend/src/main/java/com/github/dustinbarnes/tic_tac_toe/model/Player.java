package com.github.dustinbarnes.tic_tac_toe.model;

public class Player {
    private String id;
    private String username;

    public Player() {}

    public Player(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
