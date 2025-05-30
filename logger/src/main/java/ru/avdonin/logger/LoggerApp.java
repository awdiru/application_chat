package ru.avdonin.logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"ru.avdonin.template", "ru.avdonin.logger"})
public class LoggerApp {
    public static void main(String[] args) {
        SpringApplication.run(LoggerApp.class, args);
    }
}