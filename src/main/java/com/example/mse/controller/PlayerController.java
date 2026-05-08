package com.example.mse.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.example.mse.model.Player;
import com.example.mse.service.DiceBearService;
import com.example.mse.service.PlayerService;

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
    public ResponseEntity<String> getAvatarUrl(
            @RequestParam(defaultValue = "adventurer") String style,
            @RequestParam String seed) {
        String encodedSeed = URLEncoder.encode(seed, StandardCharsets.UTF_8);
        return ResponseEntity.ok(diceBearService.getAvatarUrl(style, encodedSeed));
    }

    // 이미지 직접 프록시
    @GetMapping("/image")
    public ResponseEntity<byte[]> getAvatarImage(
            @RequestParam(defaultValue = "adventurer") String style,
            @RequestParam String seed) {
        String encodedSeed = URLEncoder.encode(seed, StandardCharsets.UTF_8);
        byte[] image = diceBearService.fetchAvatar(style, encodedSeed);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }

    // Player 객체로 아바타 프로필 생성
    @GetMapping("/player")
    public ResponseEntity<Player> getPlayerWithAvatar(
            @RequestParam String name,
            @RequestParam(defaultValue = "adventurer") String style) {
        String encodedSeed = URLEncoder.encode(name, StandardCharsets.UTF_8);
        String avatarUrl = diceBearService.getAvatarUrl(style, encodedSeed);

        Player player = new Player();
        player.setName(name);
        player.setId(UUID.randomUUID().toString());
        player.setProfileUrl(avatarUrl);

        playerService.save(player);

        return ResponseEntity.ok(player);
    }

    @GetMapping("/players")
    public ResponseEntity<?> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAll());
    }
}
