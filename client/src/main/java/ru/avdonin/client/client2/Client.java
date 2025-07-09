package ru.avdonin.client.client2;

import ru.avdonin.client.client2.service.BaseClient;
import ru.avdonin.template.model.util.ActionNotification;

public class Client extends BaseClient {

    @Override
    public void onMessage(String message) throws Exception {
        ActionNotification<?> actionNotification = objectMapper.readValue(message, ActionNotification.class);
        switch (actionNotification.getAction()) {
            case MESSAGE -> {
                return;
            }
            case INVITATION -> {
                return;
            }
            case TYPING -> {
                return;
            }
            case FORWARD -> {
                return;
            }
            case READ -> {
                return;
            }
        }
    }


}
