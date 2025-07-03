package ru.avdonin.client.client.settings.time_zone;

import ru.avdonin.client.client.context.Context;
import ru.avdonin.client.client.settings.BaseFactory;
import ru.avdonin.client.repository.configs.DefaultConfigs;
import ru.avdonin.template.exceptions.FactoryException;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import static ru.avdonin.client.client.context.ContextKeysEnum.TIME_ZONE;

public class FactoryTimeZone extends BaseFactory<EnumTimeZone, BaseTimeZone> {
    private static FactoryTimeZone factory;
    private static EnumTimeZone timeZone;
    private static final String TIME_ZONE_CONFIG = DefaultConfigs.TIME_ZONE.getConfigName();

    private FactoryTimeZone() {
    }

    public static FactoryTimeZone getFactory() {
        return factory == null ? factory = new FactoryTimeZone() : factory;
    }

    @Override
    public BaseTimeZone getSettings() {
        return getFrameSettings().getTimeZone();
    }

    @Override
    public EnumTimeZone getFrameSettings() {
        if (timeZone == null) {
            timeZone = EnumTimeZone.valueOf(getProperty(TIME_ZONE_CONFIG));
            if (timeZone == EnumTimeZone.SYSTEM) timeZone = getSystemTimeZone();
        }
        return timeZone;
    }

    @Override
    public void setValue(EnumTimeZone value) {
        try {
            updateProperty(TIME_ZONE_CONFIG, value.name());
            if (value == EnumTimeZone.SYSTEM) timeZone = getSystemTimeZone();
            else timeZone = value;
            Context.put(TIME_ZONE, timeZone);
        } catch (Exception e) {
            throw new FactoryException("Failed to set time zone", e);
        }
    }

    @Override
    public void setValue(String value) {
        try {
            EnumTimeZone timeZone = null;
            for (EnumTimeZone tz : EnumTimeZone.values())
                if (tz.getSelectedSetting().equals(value)) timeZone = tz;

            if (timeZone == null) throw new RuntimeException();
            setValue(timeZone);
        } catch (Exception e) {
            throw new FactoryException("Invalid time zone: " + value, e);
        }
    }

    private static EnumTimeZone getSystemTimeZone() {
        OffsetDateTime time = OffsetDateTime.now(ZoneId.systemDefault());
        String offset = time.getOffset().getId();
        if (offset.equals("Z")) return EnumTimeZone.GMT_00_00;
        Integer offsetInt = Integer.parseInt(offset.split(":")[0]);
        for (EnumTimeZone tz : EnumTimeZone.values())
            if (tz.getTimeZoneOffset().equals(offsetInt)) return timeZone = tz;

        return EnumTimeZone.GMT_00_00;
    }
}
