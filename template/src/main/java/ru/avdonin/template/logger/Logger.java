package ru.avdonin.template.logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.avdonin.template.model.util.LogMessage;

@Service
public class Logger{
    private static final String MESSAGE_TOPIC = "logger";
    private final StackWalker walker = StackWalker.getInstance(
            StackWalker.Option.RETAIN_CLASS_REFERENCE
    );

    @Qualifier("loggerKafkaTemplate")
    private final KafkaTemplate<String, LogMessage> kafkaTemplate;

    @Autowired
    public Logger(KafkaTemplate<String, LogMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void debug(String message) {
        LogMessage logMessage = getLogMessage(message, "debug");
        kafkaTemplate.send(MESSAGE_TOPIC, null, logMessage);
    }

    public void info(String message) {
        LogMessage logMessage = getLogMessage(message, "info");
        kafkaTemplate.send(MESSAGE_TOPIC, null, logMessage);
    }

    public void warn(String message) {
        LogMessage logMessage = getLogMessage(message, "warn");
        kafkaTemplate.send(MESSAGE_TOPIC, null, logMessage);
    }

    public void error(String message) {
        LogMessage logMessage = getLogMessage(message, "error");
        kafkaTemplate.send(MESSAGE_TOPIC, null, logMessage);
    }

    private LogMessage getLogMessage(String message, String level) {
        return LogMessage.builder()
                .message(message)
                .method(method())
                .level(level)
                .stackTraceElements(Thread.currentThread().getStackTrace())
                .build();
    }

    private String method() {
        return walker.walk(frames ->
                frames.skip(3)
                        .findFirst()
                        .map(frame -> frame.getClassName()
                                + " | " + frame.getMethodName() + " ")
                        .orElse("unknown "));
    }
}
