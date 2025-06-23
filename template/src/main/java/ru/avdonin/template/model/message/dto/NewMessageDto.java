package ru.avdonin.template.model.message.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class NewMessageDto {
    private String chatId;
    private Long messageId;
    private String sender;
    private String locale;
}
