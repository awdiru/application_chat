package ru.avdonin.client.settings.time_zone;

import ru.avdonin.client.settings.BaseFactory;
import ru.avdonin.template.exceptions.FactoryException;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public class FactoryTimeZone extends BaseFactory {
    private static FactoryTimeZone factory;
    private static FrameTimeZone timeZone;
    private static final String TIME_ZONE_YML = "time_zone";

    private FactoryTimeZone() {
    }

    public static FactoryTimeZone getFactory() {
        return factory == null ? factory = new FactoryTimeZone() : factory;
    }

    @Override
    public BaseTimeZone getSettings() {
        return  getFrameSettings().getTimeZone();
    }

    @Override
    public FrameTimeZone getFrameSettings() {
        if (timeZone == null) {
            timeZone = FrameTimeZone.valueOf(getPropertyFromYml(TIME_ZONE_YML));
            if (timeZone == FrameTimeZone.SYSTEM) timeZone = getSystemTimeZone();
        }
        return timeZone;
    }

    public void setTimeZone(FrameTimeZone newTimeZone) {
        try {
            updateProperty(TIME_ZONE_YML, newTimeZone.name());
            timeZone = newTimeZone;
        } catch (Exception e) {
            throw new FactoryException("Failed to set time zone", e);
        }
    }

    public void setTimeZone(String newTimeZone) {
        try {
            FrameTimeZone timeZone = null;
            for (FrameTimeZone tz : FrameTimeZone.values())
                if (tz.getSelectedSetting().equals(newTimeZone)) timeZone = tz;
            if (timeZone == null) throw new RuntimeException();
            setTimeZone(timeZone);
        } catch (Exception e) {
            throw new FactoryException("Invalid time zone: " + newTimeZone, e);
        }
    }

    public static FrameTimeZone getSystemTimeZone() {
        OffsetDateTime time = OffsetDateTime.now(ZoneId.systemDefault());
        String offset = time.getOffset().getId();
        if (offset.equals("Z")) return FrameTimeZone.GMT_00_00;
        Integer offsetInt = Integer.parseInt(offset.split(":")[0]);
        for (FrameTimeZone tz : FrameTimeZone.values())
            if (tz.getTimeZoneOffset().equals(offsetInt)) return timeZone = tz;

        return FrameTimeZone.GMT_00_00;
    }
}
