package ru.avdonin.template.model.user.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UsernameDto {
    private String username;
    private String locale;
}
