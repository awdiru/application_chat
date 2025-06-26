package ru.avdonin.template.model.message.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class MessageDto {
    private Long id;
    private OffsetDateTime time;
    private String chatId;
    private String sender;
    @ToString.Exclude
    private String textContent;
    @ToString.Exclude
    private Set<String> imagesBase64;
    private String locale;
}