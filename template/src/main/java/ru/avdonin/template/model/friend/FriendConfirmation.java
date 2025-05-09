package ru.avdonin.template.model.friend;

import lombok.Getter;

@Getter
public enum FriendConfirmation {
    UNCONFIRMED("\uD83D\uDC4B"),
    CONFIRMED("\uD83D\uDC4D"),
    REJECTED("\uD83D\uDC4E"),
    DELETED("\uD83D\uDD95");

    private final String icon;

    FriendConfirmation(String icon) {
        this.icon = icon;
    }
}
