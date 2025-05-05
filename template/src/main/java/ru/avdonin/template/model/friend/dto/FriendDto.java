package ru.avdonin.template.model.friend.dto;

import lombok.*;
import ru.avdonin.template.model.friend.FriendConfirmation;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class FriendDto {
    private String username;

    private String friendName;

    private String customFriendName;

    private FriendConfirmation confirmation;
}
