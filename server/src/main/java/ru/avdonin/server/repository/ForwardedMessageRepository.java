package ru.avdonin.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.avdonin.server.entity_model.ForwardedMessage;

import java.util.List;

@Repository
public interface ForwardedMessageRepository extends JpaRepository<ForwardedMessage, Long> {

    @Query("""
            select fm from ForwardedMessage fm
            where fm.chat.id = :chatId
            """)
    List<ForwardedMessage> findAllMessagesChat(String chatId);
}
