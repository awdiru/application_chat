package ru.avdonin.template.model.chat.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ChatParticipantDto {
    private String chatId;
    private String username;
    private String locale;
}
