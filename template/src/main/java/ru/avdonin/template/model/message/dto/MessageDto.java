package ru.avdonin.template.model.message.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

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
    private String textContent;
    @ToString.Exclude
    private List<String> imagesBase64;
    private String locale;
}