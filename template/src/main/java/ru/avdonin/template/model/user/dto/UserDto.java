package ru.avdonin.template.model.user.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserDto {
    private Long id;
    private String username;
    private String locale;
}
