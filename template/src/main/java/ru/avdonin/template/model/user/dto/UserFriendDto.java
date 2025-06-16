package ru.avdonin.template.model.user.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserFriendDto {
    private String username;
    private String friendName;
    private String locale;
}
