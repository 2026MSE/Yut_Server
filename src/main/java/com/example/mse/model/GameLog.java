package com.example.mse.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameLog {

    private String type;
    private String message;
    private long timeMillis;

    public GameLog(String type, String message) {
        this.type = type;
        this.message = message;
        this.timeMillis = System.currentTimeMillis();
    }
}