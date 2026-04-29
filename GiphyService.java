package com.example.mse;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service

public class GiphyService {

    @Autowired
    private Giphy giphyProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<?, ?> searchGifs(String query, int limit) {
        String url = giphyProperties.getBaseUrl() + "/gifs/search?api_key="
                + giphyProperties.getKey() + "&q=" + query + "&limit=" + limit;
        return restTemplate.getForObject(url, Map.class);
    }

    public Map<?, ?> getTrendingGifs(int limit) {
        String url = giphyProperties.getBaseUrl() + "/gifs/trending?api_key="
                + giphyProperties.getKey() + "&limit=" + limit;
        return restTemplate.getForObject(url, Map.class);
    }
    
}
