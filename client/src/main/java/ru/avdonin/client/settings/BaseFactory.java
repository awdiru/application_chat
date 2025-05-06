package ru.avdonin.client.settings;

public abstract class BaseFactory {

    public static BaseFactory getFactory() {
        return null;
    }

    public abstract BaseSettings getSettings();

    public abstract FrameSettings getFrameSettings();
}
