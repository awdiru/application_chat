package ru.avdonin.testWebSocket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.avdonin.testWebSocket.exceptions.IncorrectUserDataException;
import ru.avdonin.testWebSocket.model.dto.user.UserAuthenticationDto;
import ru.avdonin.testWebSocket.model.dto.user.UserDto;
import ru.avdonin.testWebSocket.model.User;
import ru.avdonin.testWebSocket.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserService {
    private final UserRepository userRepository;

    public UserDto findById(Long id) {
        log("findUserById: id: " + id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IncorrectUserDataException("User with id " + id + " does not exist"));
        return getUserDtoFromUser(user);
    }

    public UserDto findByUsername(String username) {
        log("findByUsername: username: " + username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IncorrectUserDataException("User with username " + username + " does not exist"));
        return getUserDtoFromUser(user);
    }

    public boolean validate(UserAuthenticationDto userDto) {
        try {
            log("validate: username: " + userDto.getUsername());
            String errorLoginMessage = "Invalid username or password";

            User user = userRepository.findByUsername(userDto.getUsername())
                    .orElseThrow(() -> new IncorrectUserDataException(errorLoginMessage));

            if (!user.getPassword().equals(userDto.getPassword()))
                throw new IncorrectUserDataException(errorLoginMessage);

            return true;

        } catch (IncorrectUserDataException e) {
            log("validate: ERROR: " + e.getMessage());
            return false;
        }
    }

    public boolean save(UserAuthenticationDto userDto) {
        try {
            log("save: username: " + userDto.getUsername());
            User user = User.builder()
                    .username(userDto.getUsername())
                    .password(userDto.getPassword())
                    .build();
            user = userRepository.save(user);

            return true;
        } catch (Exception e) {
            log("save: ERROR: " + e.getMessage());
            return false;
        }
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
