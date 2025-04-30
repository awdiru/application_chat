package ru.avdonin.testWebSocket.model.dto.message;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class MessageDto {
    private LocalDateTime time;
    private String content;
    private String sender;
    private String recipient;
}
