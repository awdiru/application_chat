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
@Table(name = "chats")
public class Chat {
    @Id
    private String id;

    @Column(name = "chat_name")
    private String chatName;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;
}
