package ru.avdonin.testWebSocket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.avdonin.testWebSocket.model.dto.user.UserAuthenticationDto;
import ru.avdonin.testWebSocket.service.UserService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserController {
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @PostMapping("/signup")
    public ResponseEntity<Object> registry(@RequestBody UserAuthenticationDto userDto) {
        log("registry user: " + userDto);
        if (userService.save(userDto))
            return new ResponseEntity<>(HttpStatus.OK);
        else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserAuthenticationDto userDto) {
        log("login user: " + userDto);
        if (userService.validate(userDto))
            return new ResponseEntity<>(HttpStatus.OK);
        else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    private void log(String text) {
        System.out.println("[" + LocalDateTime.now() + "] UserController: " + text);
    }
}
