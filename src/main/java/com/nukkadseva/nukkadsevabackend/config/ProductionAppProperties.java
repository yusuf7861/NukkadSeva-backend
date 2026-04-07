package com.nukkadseva.nukkadsevabackend.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Profile("prod")
@ConfigurationProperties(prefix = "app")
@Validated
@Getter
@Setter
public class ProductionAppProperties {

    @Valid
    @NotNull
    private Db db = new Db();

    @Valid
    @NotNull
    private Jwt jwt = new Jwt();

    @Valid
    @NotNull
    private Mail mail = new Mail();

    @Valid
    @NotNull
    private Storage storage = new Storage();

    @AssertTrue(message = "app.storage.azure-connection-string is required when storage type is azure")
    public boolean isAzureStorageConfigValid() {
        if (!"azure".equalsIgnoreCase(storage.type)) {
            return true;
        }
        return storage.azureConnectionString != null && !storage.azureConnectionString.trim().isEmpty();
    }

    @Getter
    @Setter
    public static class Db {
        @NotBlank
        private String url;

        @NotBlank
        private String username;

        @NotBlank
        private String password;
    }

    @Getter
    @Setter
    public static class Jwt {
        @NotBlank
        @Size(min = 32)
        private String secretKey;
    }

    @Getter
    @Setter
    public static class Mail {
        @NotBlank
        @Pattern(regexp = "^(?!default$).+", message = "must not be default in prod")
        private String username;

        @NotBlank
        @Pattern(regexp = "^(?!default$).+", message = "must not be default in prod")
        private String password;
    }

    @Getter
    @Setter
    public static class Storage {
        @NotBlank
        private String type;

        private String azureConnectionString;
    }
}

