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
public class ChatCreateDto extends BaseDto {
    private String chatName;
    private String username;
    private Boolean privateChat;
}
