// /state/playerInfo 응답용 (상태 전달용)

package com.example.mse;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Getter
@Setter

public class PlayerInfo {

    private String playerId;

    private String icon;

    private String profileUrl;

    private String name;

    private List<String> chanceCards = new ArrayList<>();

    private String currentEmoticon;

    private boolean alive;

}
