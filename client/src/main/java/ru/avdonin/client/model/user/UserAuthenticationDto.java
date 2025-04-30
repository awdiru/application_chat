package ru.avdonin.client.model.user;

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
}
