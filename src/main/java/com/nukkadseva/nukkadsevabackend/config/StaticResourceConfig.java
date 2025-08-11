package com.nukkadseva.nukkadsevabackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get the absolute path to the uploads directory
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

        // Map /uploads/** URLs to the actual uploads directory
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath)
                .setCachePeriod(3600); // Cache for 1 hour

        System.out.println("Configured static resource handler for uploads directory: " + uploadPath);
    }
}
