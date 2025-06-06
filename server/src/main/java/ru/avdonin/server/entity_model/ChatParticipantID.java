package ru.avdonin.server.entity_model;

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
public class ChatParticipantID implements Serializable {
    @Column(name = "chat_id")
    private String chatId;

    @Column(name = "user_id")
    private Long userId;
}
