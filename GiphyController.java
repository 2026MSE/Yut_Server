package com.example.mse;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/giphy")

public class GiphyController {

    @Autowired
    private GiphyService giphyService;

    // GIF 검색
    @GetMapping("/search")
    public ResponseEntity<Map<?, ?>> searchGifs(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(giphyService.searchGifs(query, limit));
    }

    // 트렌딩 GIF
    @GetMapping("/trending")
    public ResponseEntity<Map<?, ?>> getTrendingGifs(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(giphyService.getTrendingGifs(limit));
    }
}
