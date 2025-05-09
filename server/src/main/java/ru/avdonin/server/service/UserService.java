package ru.avdonin.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.avdonin.server.model.Friend;
import ru.avdonin.server.model.FriendID;
import ru.avdonin.server.model.User;
import ru.avdonin.server.repository.FriendRepository;
import ru.avdonin.server.repository.UserRepository;
import ru.avdonin.template.exceptions.IncorrectFriendDataException;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.template.model.friend.FriendConfirmation;
import ru.avdonin.template.model.friend.dto.FriendDto;
import ru.avdonin.template.model.user.dto.UserAuthenticationDto;
import ru.avdonin.template.model.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserService {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final PasswordService passwordService;

    public UserDto findById(Long id) {
        log("findUserById: id: " + id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IncorrectUserDataException("User with id " + id + " does not exist"));
        return getUserDtoFromUser(user);
    }

    public void findByUsername(String username) {
        log("findByUsername: username: " + username);
        searchUserByUsername(username);
    }

    public void validate(UserAuthenticationDto userDto) {
        log("validate: username: " + userDto.getUsername());

        User user = searchUserByUsername(userDto.getUsername());

        if (!passwordService.matches(userDto.getPassword(), user.getPassword()))
            throw new IncorrectUserDataException("Invalid password");
    }

    @Transactional
    public void save(UserAuthenticationDto userDto) {
        try {
            log("save: username: " + userDto.getUsername());
            User user = User.builder()
                    .username(userDto.getUsername())
                    .password(passwordService.hashPassword(userDto.getPassword()))
                    .build();
            userRepository.save(user);
        } catch (Exception e) {
            throw new IncorrectUserDataException("A user with that name has already been registered");
        }
    }

    public void addFriend(String username, String friendName) {
        log("addFriend: username: " + username + "; friendName: " + friendName);
        User user = searchUserByUsername(username);
        User friendUser = searchUserByUsername(friendName);

        FriendID friendID = new FriendID(user.getId(), friendUser.getId());
        Friend friend = Friend.builder()
                .id(friendID)
                .user(user)
                .friend(friendUser)
                .friendName(friendName)
                .confirmation(FriendConfirmation.UNCONFIRMED)
                .build();

        friendID = new FriendID(friendUser.getId(), user.getId());
        Optional<Friend> friendOptional = friendRepository.findById(friendID);

        if (friendOptional.isPresent()) {
            if (friendOptional.get().getConfirmation().equals(FriendConfirmation.REJECTED))
                throw new IncorrectFriendDataException("User " + friendName + " rejected your request");
            else if (friendOptional.get().getConfirmation().equals(FriendConfirmation.DELETED))
                throw new IncorrectFriendDataException("User " + friendName + " has deleted you from friends");
            else {
            Friend friendOpt = friendOptional.get();
            friendOpt.setConfirmation(FriendConfirmation.CONFIRMED);
            friend.setConfirmation(FriendConfirmation.CONFIRMED);
            friendRepository.save(friendOpt);
            }
        }
        friendRepository.save(friend);
    }

    public void removeFriend(String username, String friendName) {
        log("removeFriend: username: " + username + "; friendName: " + friendName);
        User user = searchUserByUsername(username);
        User friendUser = searchUserByUsername(friendName);

        FriendID friendID = new FriendID(user.getId(), friendUser.getId());
        Optional<Friend> friendOptional = friendRepository.findById(friendID);

        if (friendOptional.isPresent()) {
            Friend friend = friendOptional.get();
            friend.setConfirmation(FriendConfirmation.DELETED);
            friendRepository.save(friend);
        } else throw new IncorrectFriendDataException("User " + username + " is not your friend");

        friendID = new FriendID(friendUser.getId(), user.getId());
        friendOptional = friendRepository.findById(friendID);

        if (friendOptional.isPresent()) {
            Friend friend = friendOptional.get();
            friend.setConfirmation(FriendConfirmation.DELETED);
            friendRepository.save(friend);
        }
    }

    public List<FriendDto> getFriends(String username) {
        log("getFriends: username: " + username);
        return friendRepository.findAllFriends(username).stream()
                .map(friend -> FriendDto.builder()
                        .username(friend.getUser().getUsername())
                        .friendName(friend.getFriend().getUsername())
                        .confirmation(friend.getConfirmation())
                        .customFriendName(friend.getFriendName())
                        .build())
                .toList();
    }

    public List<FriendDto> getRequestsFriends(String username) {
        log("getRequestsFriends: username: " + username);
        return friendRepository.findAllRequestFriends(username).stream()
                .map(friend -> FriendDto.builder()
                        .username(friend.getUser().getUsername())
                        .friendName(friend.getFriend().getUsername())
                        .customFriendName(friend.getFriendName())
                        .confirmation(friend.getConfirmation())
                        .build())
                .toList();
    }

    public void confirmedFriend(String username, String friendName, Boolean confirm) {
        User user = searchUserByUsername(username);
        User friendUser = searchUserByUsername(friendName);

        FriendID friendID = new FriendID(friendUser.getId(), user.getId());

        Friend friend = friendRepository.findById(friendID)
                .orElseThrow(() -> new IncorrectFriendDataException("User " + friendName + " did not send a friend request"));

        if (friend.getConfirmation().equals(FriendConfirmation.CONFIRMED) && confirm)
            throw new IncorrectFriendDataException("User " + friendName + " is already your friend");

        if (confirm) {
            FriendID friendIDNew = new FriendID(user.getId(), friendUser.getId());
            Friend friendNew = Friend.builder()
                    .id(friendIDNew)
                    .user(user)
                    .friend(friendUser)
                    .friendName(friendName)
                    .confirmation(FriendConfirmation.CONFIRMED)
                    .build();
            friendRepository.save(friendNew);

            friend.setConfirmation(FriendConfirmation.CONFIRMED);
        } else friend.setConfirmation(FriendConfirmation.REJECTED);

        friendRepository.save(friend);
    }

    public void renameFriend(String username, String friendName, String newFriendName) {
        User user = searchUserByUsername(username);
        User friendUser = searchUserByUsername(friendName);

        FriendID friendID = new FriendID(user.getId(), friendUser.getId());

        Friend friend = friendRepository.findById(friendID)
                .orElseThrow(() -> new IncorrectFriendDataException("User " + friendName + " is not a friend"));

        friend.setFriendName(newFriendName);
        friendRepository.save(friend);
    }

    private User searchUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IncorrectUserDataException("User with username " + username + " does not exist"));
    }

    private UserDto getUserDtoFromUser(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }

    private void log(String text) {
        System.out.println("[" + LocalDateTime.now() + "] UserService: " + text);
    }
}
