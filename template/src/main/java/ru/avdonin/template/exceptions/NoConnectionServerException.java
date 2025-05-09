package ru.avdonin.template.exceptions;

public class NoConnectionServerException extends RuntimeException {
    public NoConnectionServerException(String message) {
        super(message);
    }
}
