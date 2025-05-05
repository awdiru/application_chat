package ru.avdonin.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.avdonin.server.model.Friend;
import ru.avdonin.server.model.FriendID;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friend, FriendID> {
    @Query("""
            select f from Friend f
            where f.user.username = :username
            and f.confirmation = 'CONFIRMED'
            """)
    List<Friend> findAllFriends(String username);

    @Query("""
            select f from Friend f
            where f.friend.username = :username
            and f.confirmation = 'UNCONFIRMED'
            """)
    List<Friend> findAllRequestFriends(String username);
}
