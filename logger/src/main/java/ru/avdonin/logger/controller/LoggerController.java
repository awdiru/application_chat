package ru.avdonin.logger.controller;

import org.springframework.kafka.annotation.KafkaListener;
import ru.avdonin.logger.logger.Logger;
import ru.avdonin.logger.logger.LoggerLevel;
import ru.avdonin.template.model.util.LogMessage;

import java.io.IOException;

public class LoggerController {
    private final Logger logger = new Logger(LoggerLevel.INFO);

    public LoggerController() throws IOException {
    }

    @KafkaListener(topics = "logger", groupId = "logger-group")
    public void consume(LogMessage logMessage) {
        logger.log(logMessage);
    }
}
