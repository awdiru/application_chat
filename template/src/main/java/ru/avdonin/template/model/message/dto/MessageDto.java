package ru.avdonin.template.model.message.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class MessageDto {
    @EqualsAndHashCode.Exclude
    private Long id;
    @EqualsAndHashCode.Exclude
    private OffsetDateTime time;
    private String chatId;
    private String sender;
    @ToString.Exclude
    private String textContent;
    @ToString.Exclude
    private Set<String> imagesBase64;
    private Boolean edited;
    @EqualsAndHashCode.Exclude
    private String locale;
}