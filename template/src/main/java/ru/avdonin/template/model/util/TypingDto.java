package ru.avdonin.template.model.util;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingDto {
    private String chatId;
    private String username;
    private Boolean isTyping;
    private String locale;
}
