package ru.avdonin.template.model.chat.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ChatRenameDto {
    private String username;
    private String chatId;
    private String newChatName;
    private String locale;
}
