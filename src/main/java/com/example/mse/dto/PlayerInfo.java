// /state/playerInfo 응답용 (상태 전달용)

package com.example.mse.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Getter
@Setter

public class PlayerInfo {

    private String playerId;
    private String name;

    private boolean alive = true;

    private String currentEmoticon = "";

    private List<String> inventory = new ArrayList<>();

}
