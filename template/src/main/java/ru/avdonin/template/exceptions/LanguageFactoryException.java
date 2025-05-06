package ru.avdonin.template.exceptions;

public class LanguageFactoryException extends RuntimeException {
    public LanguageFactoryException(String message) {
        super(message);
    }

    public LanguageFactoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
