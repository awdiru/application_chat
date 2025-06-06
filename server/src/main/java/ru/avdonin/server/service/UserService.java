package ru.avdonin.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.avdonin.server.entity_model.*;
import ru.avdonin.server.repository.ChatParticipantRepository;
import ru.avdonin.server.repository.ChatRepository;
import ru.avdonin.server.repository.UserRepository;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.template.model.user.dto.UserAuthenticationDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public void validate(UserAuthenticationDto userDto) {

        User user = searchUserByUsername(userDto.getUsername());

        if (!passwordService.matches(userDto.getPassword(), user.getPassword()))
            throw new IncorrectUserDataException("Invalid password");
    }

    @Transactional
    public void save(UserAuthenticationDto userDto) {
        try {
            User user = User.builder()
                    .username(userDto.getUsername())
                    .password(passwordService.hashPassword(userDto.getPassword()))
                    .icon("new_user.png")
                    .build();
            userRepository.save(user);
        } catch (Exception e) {
            throw new IncorrectUserDataException("A user with that name has already been registered");
        }
    }

    public User searchUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IncorrectUserDataException("User with username " + username + " does not exist"));
    }
}
