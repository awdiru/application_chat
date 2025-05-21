package ru.avdonin.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import ru.avdonin.server.service.EncryptionService;

@SpringBootApplication
@ComponentScan(basePackages = {"ru.avdonin.template", "ru.avdonin.server"})
public class ServerApp {

	public static void main(String[] args) {
		SpringApplication.run(ServerApp.class, args);
	}
}
