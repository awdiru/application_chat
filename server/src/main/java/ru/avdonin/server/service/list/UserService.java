package ru.avdonin.server.service.list;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.avdonin.server.entity_model.User;
import ru.avdonin.server.repository.UserRepository;
import ru.avdonin.server.service.AbstractService;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.template.model.chat.dto.ChatCreateDto;
import ru.avdonin.template.model.user.dto.UserAuthenticationDto;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserService extends AbstractService {
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final ChatService chatService;

    public void validate(UserAuthenticationDto userDto) {

        User user = searchUserByUsername(userDto.getUsername(), userDto.getLocale());
        String decryptedPassword = encryptionService.decrypt(user.getPassword(), user.getUsername(), userDto.getLocale());
        if (!decryptedPassword.equals(userDto.getPassword()))
            throw new IncorrectUserDataException(
                    getDictionary(userDto.getLocale()).getValidateIncorrectUserDataException());
    }

    @Transactional
    public void save(UserAuthenticationDto userDto) {
        try {
            if (userDto.getUsername() == null || userDto.getUsername().isEmpty())
                throw new IncorrectUserDataException(getDictionary(userDto.getLocale())
                        .getSaveIncorrectLoginException());
            if (userDto.getPassword() == null || userDto.getPassword().isEmpty())
                throw new IncorrectUserDataException(getDictionary(userDto.getLocale())
                        .getSaveIncorrectPasswordException());

            String encryptedPassword = encryptionService.encrypt(
                    userDto.getPassword(),
                    userDto.getUsername(),
                    userDto.getLocale());

            User user = User.builder()
                    .username(userDto.getUsername())
                    .password(encryptedPassword)
                    .icon("new_user.png")
                    .build();

            userRepository.save(user);
            chatService.createPersonalChat(ChatCreateDto.builder()
                    .chatName(userDto.getUsername())
                    .username(userDto.getUsername())
                    .locale(userDto.getLocale())
                    .privateChat(true)
                    .build());

        } catch (IncorrectUserDataException e) {
            throw e;
        } catch (Exception e) {
            throw new IncorrectUserDataException(getDictionary(userDto.getLocale())
                    .getSaveIncorrectUserDataException());
        }
    }

    public User searchUserByUsername(String username, String locale) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IncorrectUserDataException(getDictionary(locale)
                        .getSearchUserByUsernameIncorrectUserDataException(username)));
    }
}
