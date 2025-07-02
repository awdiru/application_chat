package ru.avdonin.client.client;

import ru.avdonin.client.client.constatnts.KeysCtx;
import ru.avdonin.client.client.settings.language.FactoryLanguage;
import ru.avdonin.client.repository.ConfigsRepository;

import java.util.HashMap;
import java.util.Map;

import static ru.avdonin.client.client.constatnts.KeysCtx.*;

public class Context {
    private static final Map<String, Object> ctx = new HashMap<>();

    static {
        put(DICTIONARY, FactoryLanguage.getFactory().getSettings());
        put(CONFIG_REP, new ConfigsRepository());
        put(CLIENT, new Client());
    }

    public static void put(KeysCtx key, Object value) {
        if (!key.getValueClass().isInstance(value))
            throw new IllegalArgumentException("Invalid type for key: " + key
                    + ". Expected: " + key.getValueClass() + ", got: " + value.getClass());

        ctx.put(key.getKey(), value);
    }

    public static void put(String key, Object value) {
        ctx.put(key, value);
    }

    public static <T> T get(KeysCtx key) {
        Object value = ctx.get(key.getKey());
        if (value == null) throw new IllegalArgumentException("key " + key.getKey() + " not found");
        return (T) key.getValueClass().cast(value);
    }

    public static Object get(String key) {
        Object value = ctx.get(key);
        if (value == null) throw new IllegalArgumentException("key " + key + " not found");
        return value;
    }

    public static void remove(KeysCtx key) {
        ctx.remove(key.getKey());
    }

    public static void remove(String key) {
        ctx.remove(key);
    }
}
