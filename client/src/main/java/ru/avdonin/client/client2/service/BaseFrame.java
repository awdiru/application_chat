package ru.avdonin.client.client2.service;

import ru.avdonin.client.client.context.Context;
import ru.avdonin.client.client.context.ContextKeysEnum;
import ru.avdonin.client.client.settings.dictionary.BaseDictionary;
import ru.avdonin.client.client2.Client;

import javax.swing.*;

public class BaseFrame extends JFrame {
    protected BaseDictionary getDictionary() {
        return Context.get(ContextKeysEnum.DICTIONARY);
    }
    protected Client getClient() {
        return Context.get(ContextKeysEnum.CLIENT);
    }
    protected String getUsername() {
        return Context.get(ContextKeysEnum.USERNAME);
    }
}
