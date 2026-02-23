package com.api.tinyfarm.config;

import jakarta.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class MainConfiguration {

    @Value("${secrets.username-path}")
    private String usernameFilePath;

    @Value("${secrets.password-path}")
    private String passwordFilePath;

    private String databaseUsername;
    private String databasePassword;

    // the methods bellow will be executed once, when spring will start
    @PostConstruct
    private void checkPaths() throws FileNotFoundException {
        if (!Files.exists(Path.of(usernameFilePath))) {
            throw new FileNotFoundException("File not found: " + usernameFilePath);
        }
        if (!Files.exists(Path.of(passwordFilePath))) {
            throw new FileNotFoundException("File not found: " + passwordFilePath);
        }
    }

    public void innit() throws FileNotFoundException {
        checkPaths();
        try {
            this.databaseUsername = Files.readString(Path.of(usernameFilePath)).trim();
            this.databasePassword = Files.readString(Path.of(passwordFilePath)).trim();
        } catch (Exception e) {
            throw new RuntimeException("Error reading secret files", e);
        }
    }
}