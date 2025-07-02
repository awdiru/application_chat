package ru.avdonin.client.repository.configs;

import lombok.Getter;

@Getter
public enum DefaultConfigs {
    LANGUAGE("language", "SYSTEM"),
    TIME_ZONE("time_zone", "SYSTEM"),
    HTTP_URI("http-uri", "http://localhost:8080"),
    WS_URI("ws-uri", "ws://localhost:8080");

    private final String configName;
    private final String configValue;

    DefaultConfigs(String configName, String configValue) {
        this.configName = configName;
        this.configValue = configValue;
    }
}
