package ru.avdonin.template.model.user.dto;

import lombok.*;
import ru.avdonin.template.model.util.BaseDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserAuthenticationDto {
    private String username;
    private String password;
    private String locale;
}
