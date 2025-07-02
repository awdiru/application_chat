package ru.avdonin.server.entity_model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant time;

    @Column(name = "content")
    private String textContent;

    @Column(name = "file_name")
    private String fileNames;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @Column(name = "is_editable")
    private Boolean edited;
}
