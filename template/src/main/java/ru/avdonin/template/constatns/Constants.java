package ru.avdonin.template.constatns;

import lombok.Getter;

@Getter
public enum Constants {
    COMPRESSION_AVATAR(32),
    COMPRESSION_IMAGES(230);

    private final Object value;

    Constants(Object value) {
        this.value = value;
    }
}
