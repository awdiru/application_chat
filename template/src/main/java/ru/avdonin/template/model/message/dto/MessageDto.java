package ru.avdonin.template.model.message.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class MessageDto {
    private OffsetDateTime time;
    private String content;
    private String sender;
    private String recipient;
}
