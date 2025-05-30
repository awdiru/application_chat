package ru.avdonin.template.model.util;

import lombok.*;
import ru.avdonin.template.logger.LoggerLevel;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogMessage {
    private String message;
    private String method;
    private LoggerLevel level;
}
