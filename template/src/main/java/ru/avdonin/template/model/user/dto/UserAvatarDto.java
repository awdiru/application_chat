package ru.avdonin.template.model.user.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.avdonin.template.model.BaseDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class UserAvatarDto extends BaseDto {
    private String username;
    private String avatarBase64;
}
