package ru.avdonin.testWebSocket.exceptions;

public class IncorrectUserDataException extends RuntimeException {
    public IncorrectUserDataException(String message) {
        super(message);
    }
}
