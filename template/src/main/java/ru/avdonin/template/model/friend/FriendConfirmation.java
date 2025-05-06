package ru.avdonin.template.model.friend;

import lombok.Getter;

@Getter
public enum FriendConfirmation {
    UNCONFIRMED("🖐"),
    CONFIRMED(""),
    REJECTED("");

    private final String icon;

    FriendConfirmation(String icon) {
        this.icon = icon;
    }
}
