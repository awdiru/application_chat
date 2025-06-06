package ru.avdonin.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.avdonin.server.entity_model.Chat;

@Repository
public interface ChatRepository extends JpaRepository<Chat, String> {

}
