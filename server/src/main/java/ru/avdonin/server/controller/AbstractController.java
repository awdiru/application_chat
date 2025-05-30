package ru.avdonin.server.controller;

import org.springframework.http.ResponseEntity;
import ru.avdonin.template.logger.Logger;
import ru.avdonin.template.util.ResponseBuilder;

public class AbstractController {
    protected final Logger log;

    public AbstractController(Logger log) {
        this.log = log;
    }

    protected ResponseEntity<Object> getErrorResponse(Exception e) {
        log.warn(e.getMessage());
        return ResponseBuilder.getErrorResponse(e);
    }

    protected ResponseEntity<Object> getOkResponse(String message) {
        log.info(message);
        return ResponseBuilder.getOkResponse(message);
    }
}
