package com.example.mse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.*;

@Configuration
@ConfigurationProperties(prefix="giphy.api")
@Getter
@Setter

public class Giphy {

    private String key;
    private String baseUrl;
    
}
