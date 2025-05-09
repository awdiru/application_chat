package ru.avdonin.client.settings;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import ru.avdonin.template.exceptions.FactoryException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Базовый класс фабрики
 */
public abstract class BaseFactory {
    protected static final String CONFIG_EXTERNAL = "config/client-config.yml";
    protected static final String CONFIG_RESOURCE = "client-config.yml";

    /**
     * Возвращает выбранную настройку из enum
     *
     * @return {@link BaseSettings}
     */
    public abstract BaseSettings getSettings();

    /**
     * Возвращает enum настроек
     * @return {@link FrameSettings}
     */
    public abstract FrameSettings getFrameSettings();

    protected void saveConfig(Map<String, Object> config) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Path externalPath = Paths.get(CONFIG_EXTERNAL);
        Files.createDirectories(externalPath.getParent());
        try (Writer writer = Files.newBufferedWriter(externalPath)) {
            Yaml yaml = new Yaml(options);
            yaml.dump(config, writer);
        }
    }

    protected Map<String, Object> loadConfig() throws IOException {
        Yaml yaml = new Yaml();
        Path externalPath = Paths.get(CONFIG_EXTERNAL);
        if (Files.exists(externalPath)) {
            try (InputStream in = Files.newInputStream(externalPath)) {
                return yaml.load(in);
            }
        }
        try (InputStream in = BaseFactory.class.getClassLoader().getResourceAsStream(CONFIG_RESOURCE)) {
            if (in == null) throw new FileNotFoundException("Resource not found: " + CONFIG_RESOURCE);
            return yaml.load(in);
        }
    }

    protected void updateProperty(String key, String newValue) throws Exception {
        Map<String, Object> config = loadConfig();
        Map<String, Object> appConfig = (Map<String, Object>) config.computeIfAbsent("app", k -> new HashMap<>());
        appConfig.put(key, newValue);
        saveConfig(config);
    }

    protected String getPropertyFromYml(String propertyName) {
        try {
            Map<String, Object> config = loadConfig();
            Map<String, Object> appConfig = (Map<String, Object>) config.get("app");
            if (appConfig == null) throw new FactoryException("Missing 'app' section in config");

            String property = (String) appConfig.get(propertyName);
            if (property == null) throw new FactoryException("Missing 'app." + property + "' section in config");

            return property;
        } catch (Exception e) {
            throw new FactoryException("Failed to load time zone settings", e);
        }
    }
}
