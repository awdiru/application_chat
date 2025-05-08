package ru.avdonin.client.settings.time_zone;

import lombok.Getter;
import ru.avdonin.client.settings.BaseSettings;

@Getter
public class BaseTimeZone extends BaseSettings {
    private final String GMT;
    private Integer offset = 0;

    public BaseTimeZone(String GMT, Integer offset) {
        this.GMT = GMT;
        this.offset = offset;
    }

    public BaseTimeZone(String GMT) {
        this.GMT = GMT;
    }

    @Override
    public String getCustomization() {
        return GMT;
    }
}
