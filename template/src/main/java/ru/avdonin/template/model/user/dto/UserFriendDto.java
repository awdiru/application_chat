package ru.avdonin.template.model.user.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserFriendDto {
    String username;
    String friendName;
    String locale;
}
