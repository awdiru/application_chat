package ru.avdonin.logger.log;

import lombok.Setter;
import org.springframework.stereotype.Service;
import ru.avdonin.template.logger.LoggerLevel;
import ru.avdonin.template.model.util.LogMessage;
import ru.avdonin.template.constatns.Modules;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.avdonin.template.constatns.Modules.*;

@Service
@Setter
public class LoggerService {
    private LoggerLevel loggerLevel;

    public void log(LogMessage logMessage) {
        if (logMessage.getLevel().getValue() < loggerLevel.getValue()) return;
        String entry = "[" + time() + "]\t"
                + logMessage.getLevel().toString() + "\t"
                + logMessage.getMethod() + "\t"
                + logMessage.getMessage();
        write(entry, logMessage.getMethod());
    }

    private void write(String entry, String method) {
        String logFileName;
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String module = method.split("\\.")[2];

        if (module.equals(SERVER_MODULE_NAME.getValue()))
            logFileName = "log/server/server-" + date + ".log";
        else if (module.equals(CLIENT_MODULE_NAME.getValue()))
            logFileName = "log/client/client-" + date + ".log";
        else logFileName = "log/any/" + module + "-" + date + ".log";

        try (BufferedWriter writer = createWriter(logFileName)) {
            writer.write(entry);
            writer.newLine();

        } catch (IOException e) {
            logLoggerError(e, module);
        }
    }

    private String time() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy | hh:mm:ss:SSS"));
    }

    private BufferedWriter createWriter(String path) throws IOException {
        return Files.newBufferedWriter(
                Path.of(path),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.APPEND
        );
    }

    private void logLoggerError(Exception e, String module) {
        for (Modules m : Modules.values()) {
            if (m.getValue().equals(module)) {
                e.printStackTrace();
                return;
            }
        }
        LogMessage message = LogMessage.builder()
                .level(LoggerLevel.ERROR)
                .message(e.getMessage())
                .method("ru.avdonin.logger.log.LoggerService#write")
                .build();
        log(message);
    }
}
