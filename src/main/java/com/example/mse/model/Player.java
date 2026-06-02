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

    private String currentEmoticon; //6/2 영준 현재 띄워진 이모티콘 정보 저장

    private List<ChanceCard> inventory = new ArrayList<>();

}
