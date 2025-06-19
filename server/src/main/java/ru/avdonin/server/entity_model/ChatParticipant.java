package ru.avdonin.server.entity_model;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "chat_participants")
public class ChatParticipant {
    @EmbeddedId
    private ChatParticipantID id;

    @MapsId("chatId")
    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "custom_chat_name")
    private String customChatName;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ChatParticipantID implements Serializable {
        @Column(name = "chat_id")
        private String chatId;

        @Column(name = "user_id")
        private Long userId;
    }
}
