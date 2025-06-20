package ru.avdonin.template.model.message.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class MessageDto {
    private OffsetDateTime time;
    private String chatId;
    private String sender;
    @ToString.Exclude
    private String avatarBase64;
    @ToString.Exclude
    private String textContent;
    @ToString.Exclude
    private String imageBase64;
    private String locale;
}