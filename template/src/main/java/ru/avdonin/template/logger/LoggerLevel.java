package ru.avdonin.template.logger;

import lombok.Getter;

@Getter
public enum LoggerLevel {
    DEBUG(0),
    INFO(1),
    WARN(2),
    ERROR(3);

    private final Integer value;

    LoggerLevel(Integer value) {
        this.value = value;
    }
}
