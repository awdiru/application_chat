package ru.avdonin.client.settings;

import ru.avdonin.client.repository.ConfigsRepository;

/**
 * Базовый класс фабрики
 */
public abstract class BaseFactory {
    ConfigsRepository configsRepository = new ConfigsRepository();

    /**
     * Возвращает выбранную настройку из enum
     *
     * @return {@link BaseSettings}
     */
    public abstract BaseSettings getSettings();

    /**
     * Возвращает enum настроек
     *
     * @return {@link FrameSettings}
     */
    public abstract FrameSettings getFrameSettings();


    protected void updateProperty(String key, String newValue) {
        configsRepository.updateOrCreateConfig(key, newValue);
    }

    protected String getProperty(String propertyName) {
        return configsRepository.getConfig(propertyName);
    }
}
