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
public class ChatRenameDto extends BaseDto {
    private String username;
    private String chatId;
    private String newChatName;
}
