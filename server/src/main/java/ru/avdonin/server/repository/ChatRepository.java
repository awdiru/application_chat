package ru.avdonin.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.avdonin.server.entity_model.Chat;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, String> {
    @Query("""
            select c from Chat c
            where c.id = :chatId
            """)
    List<Chat> findChatById(String chatId);

    @Query("""
            select c from Chat c
            where c.chatName = :chatName1
            or c.chatName = :chatName2
            """)
    List<Chat> findChatByChatName(String chatName1, String chatName2);
}
