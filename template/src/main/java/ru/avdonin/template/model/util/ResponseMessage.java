package ru.avdonin.template.model.util;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMessage implements Serializable {
    private LocalDateTime time;
    private String status;
    private String message;
}