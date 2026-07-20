package com.trickshotmlg.friendnet.adapter_velocity.config;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class VelocityConfig {

    private final Map<String, Object> values;

    private VelocityConfig(Map<String, Object> values) {
        this.values = values;
    }

    public static VelocityConfig load(Path dataDirectory) {
        try {
            Files.createDirectories(dataDirectory);
            Path configPath = dataDirectory.resolve("config.yml");
            if (Files.notExists(configPath)) {
                try (InputStream resource = VelocityConfig.class.getClassLoader().getResourceAsStream("config.yml")) {
                    if (resource == null) {
                        throw new IllegalStateException("Default config.yml resource is missing.");
                    }
                    Files.copy(resource, configPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            try (InputStream inputStream = Files.newInputStream(configPath)) {
                Object loaded = new Yaml().load(inputStream);
                if (loaded instanceof Map<?, ?> loadedMap) {
                    return new VelocityConfig((Map<String, Object>) loadedMap);
                }
                return new VelocityConfig(Collections.emptyMap());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load Velocity config.yml", e);
        }
    }

    public String getString(String path, String defaultValue) {
        Object value = get(path);
        return value != null ? String.valueOf(value) : defaultValue;
    }

    public int getInt(String path, int defaultValue) {
        Object value = get(path);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        Object value = get(path);
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value != null) {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        return defaultValue;
    }

    public List<String> getStringList(String path) {
        Object value = get(path);
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(item -> item != null)
                    .map(Object::toString)
                    .toList();
        }
        return List.of();
    }

    private Object get(String path) {
        String[] parts = path.split("\\.");
        Object current = values;
        for (String part : parts) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(part);
        }
        return current;
    }
}
