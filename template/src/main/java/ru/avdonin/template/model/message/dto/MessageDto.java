package ru.avdonin.template.model.message.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.avdonin.template.model.BaseDto;

import java.time.OffsetDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = false)
public class MessageDto extends BaseDto {
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
    private Boolean isRead;
}