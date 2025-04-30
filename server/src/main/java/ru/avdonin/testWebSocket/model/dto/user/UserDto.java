package ru.avdonin.testWebSocket.model.dto.user;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserDto {
    Long id;
    String username;
}
