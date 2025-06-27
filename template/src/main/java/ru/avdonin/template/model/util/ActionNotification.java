package ru.avdonin.template.model.util;

import lombok.*;

import com.fasterxml.jackson.annotation.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionNotification<T extends ActionNotification.BaseData> {

    private Action action;
    private T data;

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
            property = "action"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Message.class, name = "MESSAGE"),
            @JsonSubTypes.Type(value = Invitation.class, name = "INVITATION"),
    })
    public interface BaseData {
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @JsonTypeName("MESSAGE")
    public static class Message implements BaseData {
        private String chatId;
        private Long messageId;
        private String sender;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @JsonTypeName("INVITATION")
    public static class Invitation implements BaseData {
        private String invitationCode;
        private String invitedUser;
    }

    public enum Action {
        MESSAGE,
        INVITATION
    }
}
