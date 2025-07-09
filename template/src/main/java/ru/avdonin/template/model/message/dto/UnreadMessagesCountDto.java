package ru.avdonin.template.model.message.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.avdonin.template.model.BaseDto;
import ru.avdonin.template.model.chat.dto.ChatDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = false)
public class UnreadMessagesCountDto extends BaseDto {
    private String chatId;
    private Long unreadMessagesCount;
}
