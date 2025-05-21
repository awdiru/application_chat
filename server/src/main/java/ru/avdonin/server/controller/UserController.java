package ru.avdonin.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.avdonin.server.service.UserService;
import ru.avdonin.template.exceptions.IncorrectFriendDataException;
import ru.avdonin.template.exceptions.IncorrectUserDataException;
import ru.avdonin.template.logger.Logger;
import ru.avdonin.template.logger.LoggerFactory;
import ru.avdonin.template.model.friend.dto.FriendDto;
import ru.avdonin.template.model.user.dto.UserAuthenticationDto;
import ru.avdonin.template.model.util.ResponseMessage;

import java.time.LocalDateTime;
import java.util.List;

import static ru.avdonin.template.model.util.ResponseBuilder.getErrorResponse;
import static ru.avdonin.template.model.util.ResponseBuilder.getOkResponse;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserController {
    private static final Logger log = LoggerFactory.getLogger();
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@RequestBody UserAuthenticationDto userDto) {
        try {
            log.info("registry user: " + userDto);
            userService.save(userDto);
            return getOkResponse("The user is registered");

        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserAuthenticationDto userDto) {
        try {
            log.info("login user: " + userDto);
            userService.validate(userDto);
            return getOkResponse("The user is logged in");

        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @PostMapping("/friends/add")
    public ResponseEntity<Object> addFriend(@RequestParam String username,
                                            @RequestParam String friendName) {
        try {
            log.info("username: " + username + "; friendName: " + friendName);
            userService.addFriend(username, friendName);
            return getOkResponse("User " + username + " added user " + friendName + " as a friend");

        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @PostMapping("/friends/remove")
    public ResponseEntity<Object> removeFriend(@RequestParam String username,
                                               @RequestParam String friendName) {
        try {
            log.info("username: " + username + "; friendName: " + friendName);
            userService.removeFriend(username, friendName);
            return getOkResponse("User " + username + " deleted user " + friendName + " from friends");

        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @GetMapping("/friends/get")
    public ResponseEntity<Object> getFriends(@RequestParam String username) {
        try {
            log.info("username: " + username);
            List<FriendDto> friends = userService.getFriends(username);
            return ResponseEntity.ok().body(friends);

        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @GetMapping("/friends/requests")
    public ResponseEntity<Object> getRequestsFriends(@RequestParam String username) {
        try {
            log.info("username: " + username);
            List<FriendDto> requestsFriends = userService.getRequestsFriends(username);
            return ResponseEntity.ok().body(requestsFriends);

        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @PostMapping("/friends/confirmed")
    public ResponseEntity<Object> confirmedFriend(@RequestParam String username,
                                                  @RequestParam String friendName,
                                                  @RequestParam Boolean confirm) {
        try {
            log.info("username: " + username + "; friendName: " + friendName);
            userService.confirmedFriend(username, friendName, confirm);

            if (confirm)
                return getOkResponse("User " + username + " has confirmed the addition of user " + friendName + " as a friend");
            else return getFriends("User " + username + " declined to add user " + friendName + " as a friend");

        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @PatchMapping("/friend/rename")
    public ResponseEntity<Object> renameFriend(@RequestParam String username,
                                               @RequestParam String friendName,
                                               @RequestParam String newFriendName) {
        try {
            log.info("username: " + username + "; friendName: " + friendName + "; newFriendName: " + newFriendName);
            userService.renameFriend(username, friendName, newFriendName);
            return getOkResponse("User " + username + " renamed user " + friendName + " to " + newFriendName);

        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }
}
