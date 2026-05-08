//찬미 룸아이디랑 플레이어 아이디, 시작 여부 보관하는 DTO 추가

package com.example.mse.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomInfo {

    private String roomId;
    private List<String> playerIds;

    private boolean started;

}