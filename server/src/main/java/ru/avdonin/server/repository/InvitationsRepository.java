package ru.avdonin.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.avdonin.server.entity_model.InvitationChat;

import java.util.List;
import java.util.Optional;

public interface InvitationsRepository extends JpaRepository<InvitationChat, Long> {

    @Query("""
            select inv from InvitationChat inv
            where inv.user.username = :username
            """)
    List<InvitationChat> findAllByUsername(String username);

    @Query("""
            select inv from InvitationChat inv
            where inv.user.username = :username
            and inv.chat.id = :chatId
            """)
    Optional<InvitationChat> findByUsernameAndChatId(String username, String chatId);
}
