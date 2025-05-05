package ru.avdonin.template.exceptions;

public class IncorrectUserDataException extends RuntimeException {
    public IncorrectUserDataException(String message) {
        super(message);
    }
}
