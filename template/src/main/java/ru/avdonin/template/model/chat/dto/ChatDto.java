package ru.avdonin.template.model.chat.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ChatDto {
    private String id;
    private String chatName;
    private String admin;
    private String customName;
}
