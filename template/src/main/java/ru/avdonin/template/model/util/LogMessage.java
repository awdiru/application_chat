package ru.avdonin.template.model.util;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LogMessage {
    private String message;
    private String method;
    private String level;
    private StackTraceElement[] stackTraceElements;
}
