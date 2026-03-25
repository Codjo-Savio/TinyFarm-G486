package com.api.tinyfarm.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final String PROPERTY_SOURCE_NAME = "tinyfarmDotenv";
    private static final Path DOTENV_PATH = Path.of(".env");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!Files.isRegularFile(DOTENV_PATH)) {
            return;
        }

        Map<String, Object> dotenvValues = loadDotenv(environment);
        if (dotenvValues.isEmpty()) {
            return;
        }

        MutablePropertySources propertySources = environment.getPropertySources();
        if (propertySources.contains(PROPERTY_SOURCE_NAME)) {
            propertySources.replace(PROPERTY_SOURCE_NAME, new MapPropertySource(PROPERTY_SOURCE_NAME, dotenvValues));
            return;
        }

        propertySources.addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, dotenvValues));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private Map<String, Object> loadDotenv(ConfigurableEnvironment environment) {
        Map<String, Object> values = new LinkedHashMap<>();

        try {
            List<String> lines = Files.readAllLines(DOTENV_PATH);
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int separatorIndex = line.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }

                String key = line.substring(0, separatorIndex).trim();
                if (key.isEmpty() || environment.containsProperty(key)) {
                    continue;
                }

                String value = normalizeValue(line.substring(separatorIndex + 1).trim());
                values.put(key, value);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read .env file", e);
        }

        return values;
    }

    private String normalizeValue(String value) {
        if (value.length() >= 2) {
            boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
            boolean singleQuoted = value.startsWith("'") && value.endsWith("'");
            if (doubleQuoted || singleQuoted) {
                return value.substring(1, value.length() - 1);
            }
        }

        return value;
    }
}
