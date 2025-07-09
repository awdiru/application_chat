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
public class InvitationChatDto extends BaseDto {
    private String chatId;
    private String chatName;
    private String username;
    private boolean confirmed;
}
