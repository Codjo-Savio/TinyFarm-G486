package com.api.tinyfarm.config;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Data
@Configuration
@Profile("prod")
public class MainConfiguration {

    @Value("${secrets.username:}")
    private String usernameFilePath;

    @Value("${secrets.password-path:}")
    private String passwordFilePath;

    private String databaseUsername;
    private String databasePassword;

    // Accept either file paths (Docker secrets style) or direct values.
    @PostConstruct
    private void init() {
        this.databaseUsername = resolveValue(usernameFilePath);
        this.databasePassword = resolveValue(passwordFilePath);
    }

    private String resolveValue(String valueOrPath) {
        if (valueOrPath == null || valueOrPath.isBlank()) {
            return "";
        }

        Path candidatePath = Path.of(valueOrPath);
        if (Files.exists(candidatePath)) {
            try {
                return Files.readString(candidatePath).trim();
            } catch (Exception e) {
                throw new RuntimeException("Error reading secret file: " + valueOrPath, e);
            }
        }

        return valueOrPath.trim();
    }
}