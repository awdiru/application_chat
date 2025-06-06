package ru.avdonin.template.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.avdonin.template.exceptions.*;

import ru.avdonin.template.model.util.ResponseMessage;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ResponseBuilder {

    public static ResponseEntity<Object> getOkResponse(String message) {
        ResponseMessage responseMessage = ResponseMessage.builder()
                .time(LocalDateTime.now())
                .message(message)
                .status(HttpStatus.OK.toString())
                .build();
        return ResponseEntity.ok().body(responseMessage);
    }

    public static ResponseEntity<Object> getErrorResponse(Exception e) {
        HttpStatus status = getErrorStatus(e);
        ResponseMessage responseMessage = ResponseMessage.builder()
                .time(LocalDateTime.now())
                .status(status.toString())
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(status).body(responseMessage);
    }

    private static HttpStatus getErrorStatus(Exception e) {
        if (e instanceof EmptyFileException
                || e instanceof IncorrectUserDataException
                || e instanceof IncorrectFriendDataException
                || e instanceof IncorrectChatDataException)
            return HttpStatus.BAD_REQUEST;

        else if (e instanceof FtpClientException)
            return HttpStatus.EXPECTATION_FAILED;

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
