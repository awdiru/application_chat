package ru.avdonin.template.model.message.dto;

import lombok.*;

import java.io.InputStream;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class MessageDto {
    private OffsetDateTime time;
    @ToString.Exclude
    private String content;
    private String sender;
    private String chatId;
    private String file;
    private String avatar;
    private String locale;
}