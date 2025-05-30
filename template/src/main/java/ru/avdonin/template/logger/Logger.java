package ru.avdonin.template.logger;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.avdonin.template.model.util.LogMessage;

import static ru.avdonin.template.constatns.KafkaTopics.LOGGER_TOPIC;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class Logger {
    private final StackWalker walker = StackWalker.getInstance(
            StackWalker.Option.RETAIN_CLASS_REFERENCE
    );

    private final KafkaTemplate<String, LogMessage> kafkaTemplate;

    public void debug(String message) {
        LogMessage logMessage = getLogMessage(message, LoggerLevel.DEBUG);
        kafkaTemplate.send(LOGGER_TOPIC.getValue(), null, logMessage);
    }

    public void info(String message) {
        LogMessage logMessage = getLogMessage(message, LoggerLevel.INFO);
        kafkaTemplate.send(LOGGER_TOPIC.getValue(), null, logMessage);
    }

    public void warn(String message) {
        LogMessage logMessage = getLogMessage(message, LoggerLevel.WARN);
        kafkaTemplate.send(LOGGER_TOPIC.getValue(), null, logMessage);
    }

    public void error(String message) {
        LogMessage logMessage = getLogMessage(message, LoggerLevel.ERROR);
        kafkaTemplate.send(LOGGER_TOPIC.getValue(), null, logMessage);
    }

    private LogMessage getLogMessage(String message, LoggerLevel level) {
        return LogMessage.builder()
                .message(message)
                .method(getMethod())
                .level(level)
                .build();
    }

    private String getMethod() {
        return walker.walk(frames ->
                frames.skip(3)
                        .findFirst()
                        .map(frame ->
                                frame.getClassName() + "#" + frame.getMethodName())
                        .orElse("unknown"));
    }
}
