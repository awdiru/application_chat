package ru.avdonin.client.repository.configs;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum DefaultConfigs {
    LANGUAGE("language", null),
    TIME_ZONE("time_zone", null),
    HTTP_URI("http-uri", "http://localhost:8080"),
    WS_URI("ws-uri", "ws://localhost:8080");

    private final String configName;
    private final String configValue;

    DefaultConfigs(String configName, String configValue) {
        this.configName = configName;
        this.configValue = Objects.requireNonNullElse(configValue, "SYSTEM");
    }
}
