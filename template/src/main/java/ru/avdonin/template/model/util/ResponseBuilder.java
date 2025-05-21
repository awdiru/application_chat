package ru.avdonin.template.model.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.avdonin.template.exceptions.EmptyFileException;
import ru.avdonin.template.exceptions.FtpClientException;
import ru.avdonin.template.logger.Logger;
import ru.avdonin.template.logger.LoggerFactory;

import java.time.LocalDateTime;

@Component
public class ResponseBuilder {
    private static final Logger log = LoggerFactory.getLogger();

    public static ResponseEntity<Object> getOkResponse(String message) {
        ResponseMessage responseMessage = ResponseMessage.builder()
                .time(LocalDateTime.now())
                .message(message)
                .status(HttpStatus.OK)
                .build();
        return ResponseEntity.ok().body(responseMessage);
    }

    public static ResponseEntity<Object> getErrorResponse(Exception e) {
        log.warn(e.getMessage());
        HttpStatus status = getErrorStatus(e);
        ResponseMessage responseMessage = ResponseMessage.builder()
                .time(LocalDateTime.now())
                .status(status)
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(status).body(responseMessage);
    }

    private static HttpStatus getErrorStatus(Exception e) {
        if (e instanceof EmptyFileException) return HttpStatus.BAD_REQUEST;
        else if (e instanceof FtpClientException) return HttpStatus.CONFLICT;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
