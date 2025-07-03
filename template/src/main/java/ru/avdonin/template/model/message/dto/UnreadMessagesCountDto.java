package ru.avdonin.template.model.message.dto;

import lombok.*;
import ru.avdonin.template.model.chat.dto.ChatDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class UnreadMessagesCountDto {
    private String chatId;
    private Long unreadMessagesCount;
    private String locale;
}
