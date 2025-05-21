package ru.avdonin.logger.logger;

import org.springframework.stereotype.Service;
import ru.avdonin.template.model.util.LogMessage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class Logger {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Integer loggerLevel;
    private BufferedWriter writer;
    private LocalDate date;

    public Logger(LoggerLevel level) throws IOException {
        this.date = LocalDate.now();
        this.writer = createWriter();
        this.loggerLevel = switch (level) {
            case DEBUG -> 0;
            case INFO -> 1;
            case WARN -> 2;
            case ERROR -> 3;
        };
    }

    public Logger() throws IOException {
        this.date = LocalDate.now();
        this.writer = createWriter();
        this.loggerLevel = 1;
    }

    public void log(LogMessage logMessage) {
        switch (logMessage.getLevel()) {
            case "debug":
                debug(logMessage);
                break;
            case "warn":
                warn(logMessage);
                break;
            case "error":
                error(logMessage);
                break;
            default:
                info(logMessage);
                break;
        }
    }

    public void debug(LogMessage logMessage) {
        if (loggerLevel > 0) return;
        String entry = time() + "DEBUG " + logMessage.getMethod() + logMessage.getMessage();
        write(entry);
    }

    public void info(LogMessage logMessage) {
        if (loggerLevel > 1) return;
        String entry = time() + "INFO " + logMessage.getMethod() + logMessage.getMessage();
        write(entry);
    }

    public void warn(LogMessage logMessage) {
        if (loggerLevel > 2) return;
        String entry = time() + "WARN " + logMessage.getMethod() + logMessage.getMessage();
        write(entry);
    }

    public void error(LogMessage logMessage) {
        if (loggerLevel > 3) return;
        String entry = time() + "WARN " + logMessage.getMethod() + logMessage.getMessage();
        write(entry);
    }

    private void write(String entry) {
        try {
            synchronized (this) {
                writer.write(entry);
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String time() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("[yyyy.MM.dd | hh : mm : ss : SSS] "));
    }


    private BufferedWriter createWriter() throws IOException {
        String filename = "log-" + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log";
        String path = "log/" + filename;
        return Files.newBufferedWriter(
                Path.of(path),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }
}
