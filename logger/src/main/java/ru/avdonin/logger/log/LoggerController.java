package ru.avdonin.logger.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RestController;
import ru.avdonin.template.logger.LoggerLevel;
import ru.avdonin.template.model.util.LogMessage;

@RestController
public class LoggerController {
    private final LoggerService loggerService;

    @Autowired
    public LoggerController(LoggerService loggerService,
                            @Value("${logger.level}") String logLevel) {
        this.loggerService = loggerService;
        this.loggerService.setLoggerLevel(LoggerLevel.valueOf(logLevel == null ? "INFO" : logLevel));
    }

    @KafkaListener(topics = "logger", groupId = "logger-group")
    public void consume(LogMessage logMessage) {
        loggerService.log(logMessage);
    }
}
