package com.example.mse.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.ApiResponse;
import com.example.mse.model.Player;
import com.example.mse.service.DiceBearService;
import com.example.mse.service.PlayerService;

import com.example.mse.dto.EmoticonRequest; // DTO import 추가 영준 6/2

@RestController
@RequestMapping("/api/avatar")
@CrossOrigin(origins = "*")

public class PlayerController {

    @Autowired
    private DiceBearService diceBearService;

    @Autowired
    private PlayerService playerService;

    // URL만 반환
    @GetMapping("/url")
    public ApiResponse<String> getAvatarUrl(
            @RequestParam(defaultValue = "adventurer") String style,
            @RequestParam String seed) {

        String encodedSeed = URLEncoder.encode(seed, StandardCharsets.UTF_8);
        String avatarUrl = diceBearService.getAvatarUrl(style, encodedSeed);

        return ApiResponse.ok("Avatar URL loaded.", avatarUrl);
    }

    // Player 객체로 아바타 프로필 생성
    @GetMapping("/player")
    public ApiResponse<Player> getPlayerWithAvatar(
            @RequestParam String name,
            @RequestParam(defaultValue = "adventurer") String style) {

        String encodedSeed = URLEncoder.encode(name, StandardCharsets.UTF_8);
        String avatarUrl = diceBearService.getAvatarUrl(style, encodedSeed);

        Player player = new Player();
        player.setName(name);
        player.setId(UUID.randomUUID().toString());
        player.setProfileUrl(avatarUrl);

        playerService.save(player);

        return ApiResponse.ok("Player created.", player);
    }

    @GetMapping("/players")
    public ApiResponse<Map<String, Player>> getAllPlayers() {
        return ApiResponse.ok("All players loaded.", playerService.getAll());
    }
//6/2 유니티에서 이모티콘 선택 시 호출할 rest api를 만들어줌
    @PostMapping("/emoticon")
    public ApiResponse<Void> updateEmoticon(@RequestBody EmoticonRequest request) {
        playerService.updateEmoticon(request.getPlayerId(), request.getEmoticonUrl());
        return ApiResponse.ok("Emoticon updated.", null);
    }
}
