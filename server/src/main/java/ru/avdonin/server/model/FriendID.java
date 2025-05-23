package ru.avdonin.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FriendID implements Serializable {
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "friend_id")
    private Long friendId;
}
