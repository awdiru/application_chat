package ru.avdonin.template.model.util;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ActionNotification {
    private Action action;
    private Object data;
    private String locale;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    public static class Message  {
        private String chatId;
        private Long messageId;
        private String sender;
        private String locale;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    public static class Invitation {
        private String locale;
    }

    public enum Action {
        MESSAGE,
        INVITATION
    }
}
