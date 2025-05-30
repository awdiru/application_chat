package ru.avdonin.template.constatns;

import lombok.Getter;

@Getter
public enum KafkaTopics {
    LOGGER_TOPIC("logger");

    private final String value;

    KafkaTopics(String value) {
        this.value = value;
    }
}
