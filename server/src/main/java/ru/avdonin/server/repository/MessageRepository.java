package ru.avdonin.server.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.avdonin.server.entity_model.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("""
            select m from Message m
            where (m.sender.username = :sender and m.recipient.username = :recipient)
            or (m.sender.username = :recipient and m.recipient.username = :sender)
            order by time desc
            """)
    List<Message> findAllMessagesUsers(String sender, String recipient, Pageable page);
}
