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
public class ChatDto extends BaseDto {
    private String id;
    private String chatName;
    private String admin;
    private String customName;
    private Boolean privateChat;
}
