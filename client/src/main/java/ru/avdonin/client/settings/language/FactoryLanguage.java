package ru.avdonin.client.settings.language;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import ru.avdonin.client.settings.BaseFactory;
import ru.avdonin.template.exceptions.FactoryException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FactoryLanguage extends BaseFactory {
    private static final String CONFIG_RESOURCE = "client-config.yml";
    private static final String CONFIG_EXTERNAL = "config/client-config.yml";
    private static FactoryLanguage factory;
    private static FrameLanguage language;

    private FactoryLanguage() {
    }

    public static FactoryLanguage getFactory() {
        return factory == null ? factory = new FactoryLanguage() : factory;
    }

    @Override
    public BaseDictionary getSettings() {
        if (language != null) return language.getLanguage();
        language = getLanguagesListFromYml();
        return language.getLanguage();
    }

    @Override
    public FrameLanguage getFrameSettings() {
        return language == null ? language = getLanguagesListFromYml() : language;
    }

    public void setLanguage(FrameLanguage language) {
        try {
            updateLanguageProperty(language);
            FactoryLanguage.language = language;
        } catch (Exception e) {
            throw new FactoryException("Failed to set language", e);
        }
    }

    public void setLanguage(String newLanguage) {
        try {
            FrameLanguage language = null;
            for (FrameLanguage l : FrameLanguage.values()) if (l.getSelectedSetting().equals(newLanguage)) language = l;
            if (language == null) throw new RuntimeException();
            setLanguage(language);
        } catch (IllegalArgumentException e) {
            throw new FactoryException("Invalid language: " + newLanguage, e);
        }
    }

    private void updateLanguageProperty(FrameLanguage newLanguage) throws Exception {
        Map<String, Object> config = loadConfig();
        Map<String, Object> appConfig = (Map<String, Object>) config.computeIfAbsent("app", k -> new HashMap<>());
        appConfig.put("language", newLanguage.name());
        saveConfig(config);
    }

    private FrameLanguage getLanguagesListFromYml() {
        try {
            Map<String, Object> config = loadConfig();
            Map<String, Object> appConfig = (Map<String, Object>) config.get("app");
            if (appConfig == null) throw new FactoryException("Missing 'app' section in config");

            String languageStr = (String) appConfig.get("language");
            if (languageStr == null) throw new FactoryException("Missing 'app.language' in config");

            return FrameLanguage.valueOf(languageStr);
        } catch (Exception e) {
            throw new FactoryException("Failed to load language settings", e);
        }
    }

    private Map<String, Object> loadConfig() throws IOException {
        Yaml yaml = new Yaml();
        Path externalPath = Paths.get(CONFIG_EXTERNAL);
        if (Files.exists(externalPath)) {
            try (InputStream in = Files.newInputStream(externalPath)) {
                return yaml.load(in);
            }
        }
        try (InputStream in = FactoryLanguage.class.getClassLoader().getResourceAsStream(CONFIG_RESOURCE)) {
            if (in == null) throw new FileNotFoundException("Resource not found: " + CONFIG_RESOURCE);
            return yaml.load(in);
        }
    }

    private void saveConfig(Map<String, Object> config) throws IOException {
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
}