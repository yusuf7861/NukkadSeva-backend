package com.nukkadseva.nukkadsevabackend.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;

public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ZaikaBox API")
                        .version("v1")
                        .description("This is the API documentation for the NukkadSeva application built using Spring Boot.")
                        .contact(new Contact()
                                .name("Yusuf Jamal")
                                .email("yjamal710@gmail.com"))
                )
                .externalDocs(new ExternalDocumentation()
                        .description("Project Docs")
                        .url("https://github.com/yusuf7861/NukkadSeva"));
    }
}
