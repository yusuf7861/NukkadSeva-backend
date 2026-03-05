package com.nukkadseva.nukkadsevabackend.config;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleOAuthConfig {

    @Bean
    public NetHttpTransport netHttpTransport () {
        return new NetHttpTransport();
    }

    @Bean
    public GsonFactory gsonFactory () {
        return GsonFactory.getDefaultInstance();
    }
}
