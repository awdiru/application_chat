package ru.avdonin.template.model.message.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.avdonin.template.model.BaseDto;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NewMessageDto.class, name = "MESSAGE"),
        @JsonSubTypes.Type(value = ForwardedMessageDto.class, name = "FORWARD"),
})
public class BaseMessageDto extends BaseDto {
    @EqualsAndHashCode.Exclude
    private Long id;
    private String chatId;
    private String sender;
    @EqualsAndHashCode.Exclude
    protected OffsetDateTime time;
}
