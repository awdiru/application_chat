package ru.avdonin.server.entity_model;


import jakarta.persistence.*;
import lombok.*;

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
}
