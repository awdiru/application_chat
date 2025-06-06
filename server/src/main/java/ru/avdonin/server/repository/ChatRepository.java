package ru.avdonin.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.avdonin.server.entity_model.Chat;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, String> {

    @Query("""
            select c from Chat c
            where c.id = :chatId
            """)
    List<Chat> findChatById(String chatId);
}
