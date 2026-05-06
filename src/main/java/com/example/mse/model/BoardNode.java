package com.example.mse.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardNode {
    private int id; //0~28번
    private Integer nextNodeId; // 직진 시 다음 칸 번호
    private Integer fastNodeId; // 모서리나 중앙에서 꺾을 때 지름길 칸 번호

    public BoardNode(int id){
        this.id = id;
    }

}
