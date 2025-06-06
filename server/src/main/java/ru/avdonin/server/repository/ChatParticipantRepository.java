package ru.avdonin.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.avdonin.server.entity_model.ChatParticipant;
import ru.avdonin.server.entity_model.ChatParticipantID;

import java.util.List;

@Repository
public interface ChatParticipantRepository extends JpaRepository <ChatParticipant, ChatParticipantID> {

    @Query("""
            select cp from ChatParticipant cp
            where cp.chat.id = :chatId
            """)
    List<ChatParticipant> findAllParticipant(String chatId);

    @Query("""
            select cp from ChatParticipant cp
            where cp.user.username = :username
            order by cp.user.username desc
            """)
    List<ChatParticipant> findAllChatsUser(String username);

    @Query("""
            delete from ChatParticipant cp
            where cp.chat.id = :chatId and cp.user.username = :username
            """)
    void logoutOfChat (String chatId, String username);
}
