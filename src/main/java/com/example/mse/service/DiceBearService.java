package com.example.mse.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DiceBearService {

    private static final String BASE_URL = "https://api.dicebear.com/9.x";
    private final RestTemplate restTemplate = new RestTemplate();

    public String getAvatarUrl(String style, String seed) {
        return BASE_URL + "/" + style + "/svg?seed=" + seed;
    }

    public byte[] fetchAvatar(String style, String seed) {
        String url = getAvatarUrl(style, seed);
        return restTemplate.getForObject(url, byte[].class);
    }
}
