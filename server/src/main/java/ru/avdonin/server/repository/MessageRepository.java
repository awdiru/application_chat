package ru.avdonin.server.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.avdonin.server.entity_model.Message;

import java.time.Instant;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("""
            select m from Message m
            where (m.chat.id = :chatId)
            order by time desc
            """)
    List<Message> findAllMessagesChat(String chatId, Pageable page);

    List<Message> findAllByTime(Instant time);

    @Query("""
            select count (m) from Message m
            where (m.chat.id = :chatId)
            and (m.isRead = false or m.isRead is null)
            """)
    long getUnreadMessagesCount(String chatId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Message m
            set m.isRead = true
            where (m.chat.id = :chatId)
            and (m.isRead = false)
            """)
    void readMessages(@Param("chatId") String chatId);
}
