package com.example.mse.service;

import org.springframework.stereotype.Service;

@Service
public class DiceBearService {

    private static final String BASE_URL = "https://api.dicebear.com/9.x";

    public String getAvatarUrl(String style, String seed) {
        return BASE_URL + "/" + style + "/png?seed=" + seed;
    }
}