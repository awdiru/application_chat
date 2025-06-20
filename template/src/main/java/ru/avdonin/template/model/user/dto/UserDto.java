package ru.avdonin.template.model.user.dto;

import lombok.*;
import ru.avdonin.template.model.util.BaseDto;

import java.io.InputStream;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserDto {
    private Long id;
    private String username;
    private InputStream icon;
    private String locale;
}
