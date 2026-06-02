package com.example.mse.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmoticonRequest {
    private String playerId;
    private String emoticonUrl; // Giphy에서 받은 GIF
}
