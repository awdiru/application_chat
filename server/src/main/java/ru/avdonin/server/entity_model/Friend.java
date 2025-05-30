package ru.avdonin.server.entity_model;

import jakarta.persistence.*;
import lombok.*;
import ru.avdonin.template.model.friend.FriendConfirmation;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "friends")
public class Friend {
    @EmbeddedId
    private FriendID id;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("friendId")
    @ManyToOne
    @JoinColumn(name = "friend_id")
    private User friend;

    @Column(name = "friend_name")
    private String friendName;

    @Enumerated(EnumType.STRING)
    @Column(name = "confirmation")
    private FriendConfirmation confirmation;
}
