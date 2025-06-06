package ru.avdonin.template.model.chat.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ChatGetHistoryDto {
    private String chatId;
    private int from;
    private int size;
}
