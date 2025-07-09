package ru.avdonin.template.model.chat.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.avdonin.template.model.BaseDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class ChatParticipantDto extends BaseDto {
    private String chatId;
    private String username;
}
