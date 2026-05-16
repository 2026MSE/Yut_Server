package com.example.mse.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MoveListResponse {

    private List<MoveGroup> moveGroups;

    public MoveListResponse(List<MoveGroup> moveGroups) {
        this.moveGroups = moveGroups;
    }
}