package ru.avdonin.template.model.util;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ResponseMessage {
    private LocalDateTime time;
    private HttpStatus status;
    private String message;
}