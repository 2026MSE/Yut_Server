// 플레이 기본 정보

package com.example.mse.model;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Getter
@Setter

public class Player {

    private String name;
    private String profileUrl;
    private String id; // 'Id' → 'id' (Java 네이밍 컨벤션)

    private List<ChanceCard> inventory = new ArrayList<>();

}
