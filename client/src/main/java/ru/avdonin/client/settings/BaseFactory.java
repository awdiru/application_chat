package ru.avdonin.client.settings;

/**
 * Базовый класс фабрики
 */
public abstract class BaseFactory {
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

}
