package ru.avdonin.template.model.message.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
@JsonTypeName("MESSAGE")
public class NewMessageDto extends BaseMessageDto {
    @ToString.Exclude
    private String textContent;
    @ToString.Exclude
    private Set<String> imagesBase64;
    private Boolean edited;
    private Boolean isRead;
}