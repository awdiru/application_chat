package ru.avdonin.template.model.chat.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class InvitationChatDto {
    private String chatId;
    private String chatName;
    private String username;
    private String chatKey;
    private boolean confirmed;
    private String locale;
}
