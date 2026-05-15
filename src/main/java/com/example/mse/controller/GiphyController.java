package com.example.mse.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.ApiResponse;
import com.example.mse.service.GiphyService;

@RestController
@RequestMapping("/api/giphy")
@CrossOrigin(origins = "*")
public class GiphyController {

    @Autowired
    private GiphyService giphyService;

    @GetMapping("/search")
    public ApiResponse<Map<?, ?>> searchGifs(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {

        return ApiResponse.ok(
                "Giphy search loaded.",
                giphyService.searchGifs(query, limit));
    }

    @GetMapping("/trending")
    public ApiResponse<Map<?, ?>> getTrendingGifs(
            @RequestParam(defaultValue = "10") int limit) {

        return ApiResponse.ok(
                "Trending gifs loaded.",
                giphyService.getTrendingGifs(limit));
    }
}