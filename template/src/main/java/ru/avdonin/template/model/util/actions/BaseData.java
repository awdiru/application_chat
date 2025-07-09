package ru.avdonin.template.model.util.actions;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.avdonin.template.model.util.actions.list.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "action"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MessageAct.class, name = "MESSAGE"),
        @JsonSubTypes.Type(value = ForwardAct.class, name = "FORWARD"),
        @JsonSubTypes.Type(value = InvitationAct.class, name = "INVITATION"),
        @JsonSubTypes.Type(value = TypingAct.class, name = "TYPING"),
        @JsonSubTypes.Type(value = ReadAct.class, name = "READ"),
})
public interface BaseData {
}
