package ru.avdonin.template.model.util.actions.list;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import ru.avdonin.template.model.util.actions.BaseData;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonTypeName("TYPING")
public class TypingAct implements BaseData {
    private String chatId;
    private String username;
    private Boolean isTyping;
}
