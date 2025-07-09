package ru.avdonin.template.model.message.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
@JsonTypeName("FORWARD")
public class ForwardedMessageDto extends BaseMessageDto {
    private NewMessageDto message;
}
