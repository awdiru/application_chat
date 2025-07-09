package ru.avdonin.client.client2.context;

import ru.avdonin.client.client2.Client;
import ru.avdonin.client.repository.ConfigsRepository;

import java.util.HashMap;
import java.util.Map;

import static ru.avdonin.client.client2.context.ContextKeysEnum.*;

public class Context {
    private static final Map<String, Object> ctx = new HashMap<>();

    static {
        put(CONFIG_REP, new ConfigsRepository());
        put(CLIENT, new Client());
    }

    public static void put(ContextKeysEnum key, Object value) {
        if (!key.getAClass().isInstance(value))
            throw new IllegalArgumentException("Invalid type for key: " + key
                    + ". Expected: " + key.getAClass() + ", got: " + value.getClass());

        ctx.put(key.getKey(), value);
    }

    public static void put(String key, Object value) {
        ctx.put(key, value);
    }

    public static <T> T get(ContextKeysEnum key) {
        Object value = ctx.get(key.getKey());
        if (value == null) throw new IllegalArgumentException("key " + key.getKey() + " not found");
        return (T) key.getAClass().cast(value);
    }

    public static Object get(String key) {
        Object value = ctx.get(key);
        if (value == null) throw new IllegalArgumentException("key " + key + " not found");
        return value;
    }

    public static void remove(ContextKeysEnum key) {
        ctx.remove(key.getKey());
    }

    public static void remove(String key) {
        ctx.remove(key);
    }
}
