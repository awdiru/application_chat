package ru.avdonin.template.model.util.actions.list;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import ru.avdonin.template.model.util.actions.BaseData;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonTypeName("INVITATION")
public class InvitationAct implements BaseData {
    private String invitationCode;
    private String invitedUser;
}
