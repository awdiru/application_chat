package ru.avdonin.template.model.message.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageDto<M extends BaseMessageDto> {
    private Type type;
    private M data;

    public enum Type {
        MESSAGE,
        FORWARD
    }
}
