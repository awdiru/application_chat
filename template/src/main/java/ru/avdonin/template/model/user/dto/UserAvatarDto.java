package ru.avdonin.template.model.user.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserAvatarDto {
    private String username;
    private String avatarBase64;
    private String locale;
}
