package ru.avdonin.template.model.util;

import lombok.*;

import com.fasterxml.jackson.annotation.*;
import ru.avdonin.template.model.util.actions.Actions;
import ru.avdonin.template.model.util.actions.BaseData;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionNotification<T extends BaseData> {

    private Actions action;
    private T data;
}
