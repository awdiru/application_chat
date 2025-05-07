package ru.avdonin.client.settings.time_zone;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import ru.avdonin.client.settings.BaseFactory;
import ru.avdonin.client.settings.FrameSettings;
import ru.avdonin.client.settings.language.FactoryLanguage;
import ru.avdonin.client.settings.language.FrameLanguage;
import ru.avdonin.template.exceptions.FactoryException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class FactoryTimeZone extends BaseFactory {
    private static final String CONFIG_RESOURCE = "client-config.yml";
    private static final String CONFIG_EXTERNAL = "config/client-config.yml";

    protected static FactoryTimeZone factory;
    private static FrameTimeZone timeZone;

    private FactoryTimeZone() {
    }

    public static FactoryTimeZone getFactory() {
        return factory == null ? factory = new FactoryTimeZone() : factory;
    }

    @Override
    public BaseTimeZone getSettings() {
        if (timeZone != null) return timeZone.getTimeZone();
        timeZone = getTimeZoneListFromYml();
        return timeZone.getTimeZone();
    }

    @Override
    public FrameSettings getFrameSettings() {
        return timeZone;
    }

    public void setTimeZone(FrameTimeZone timeZone) {
        try {
            updateTimeZoneProperty(timeZone);
            FactoryTimeZone.timeZone = timeZone;
        } catch (Exception e) {
            throw new FactoryException("Failed to set time zone", e);
        }
    }

    public void setTimeZone(String newTimeZone) {
        try {
            FrameTimeZone timeZone = null;
            for (FrameTimeZone tz : FrameTimeZone.values()) if (tz.getSelectedSetting().equals(newTimeZone)) timeZone = tz;
            if (timeZone == null) throw new RuntimeException();
            setTimeZone(timeZone);
        } catch (IllegalArgumentException e) {
            throw new FactoryException("Invalid time zone: " + newTimeZone, e);
        }
    }

    public BaseTimeZone getSystemTimeZone() {
        OffsetDateTime time = OffsetDateTime.now();
        String offset = time.getOffset().getId();
        if (offset.equals("Z")) return new BaseTimeZone("GMT", 0);
        return new BaseTimeZone("GMT " + offset, Integer.parseInt(offset.split(":")[0]));
    }

    private void updateTimeZoneProperty(FrameTimeZone newTimeZone) throws Exception {
        Map<String, Object> config = loadConfig();
        Map<String, Object> appConfig = (Map<String, Object>) config.computeIfAbsent("app", k -> new HashMap<>());
        appConfig.put("time_zone", newTimeZone.name());
        saveConfig(config);
    }

    private FrameTimeZone getTimeZoneListFromYml() {
        try {
            Map<String, Object> config = loadConfig();
            Map<String, Object> appConfig = (Map<String, Object>) config.get("app");
            if (appConfig == null) throw new FactoryException("Missing 'app' section in config");

            String timeZone = (String) appConfig.get("time_zone");
            if (timeZone == null) throw new FactoryException("Missing 'app.time_zone' in config");

            return FrameTimeZone.valueOf(timeZone);
        } catch (Exception e) {
            throw new FactoryException("Failed to load time zone settings", e);
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
        try (InputStream in = FactoryTimeZone.class.getClassLoader().getResourceAsStream(CONFIG_RESOURCE)) {
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
