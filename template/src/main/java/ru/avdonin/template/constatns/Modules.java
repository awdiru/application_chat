package ru.avdonin.template.constatns;

import lombok.Getter;

@Getter
public enum Modules {
    SERVER_MODULE_NAME("server"),
    CLIENT_MODULE_NAME("client"),
    LOGGER_MODULE_NAME("logger");

    private final String value;

    Modules(String value) {
        this.value = value;
    }
}
