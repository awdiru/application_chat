package ru.avdonin.template.model.user.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserAuthenticationDto {
    String username;
    String password;
    String locale;
}
