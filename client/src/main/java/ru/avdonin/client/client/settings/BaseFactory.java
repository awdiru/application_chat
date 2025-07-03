package ru.avdonin.client.client.settings;

import ru.avdonin.client.repository.ConfigsRepository;

/**
 * Базовый класс фабрики настроек
 */
public abstract class BaseFactory<E extends Enum<E> & EnumSettings, B extends BaseSettings> {
    ConfigsRepository configsRepository = new ConfigsRepository();

    /**
     * Возвращает выбранную настройку из enum
     *
     * @return {@link BaseSettings}
     */
    public abstract B getSettings();

    /**
     * Возвращает enum настроек
     *
     * @return {@link EnumSettings}
     */
    public abstract E getFrameSettings();

    /**
     * Установить новой значение настройки
     *
     * @param value новое значение
     */
    public abstract void setValue(E value);

    /**
     * Установить новой значение настройки
     *
     * @param value новое значение
     */
    public abstract void setValue(String value);

    /**
     * Обновить значение настройки в БД
     *
     * @param key      ключ настройки
     * @param newValue новое значение
     */
    protected void updateProperty(String key, String newValue) {
        configsRepository.updateOrCreateConfig(key, newValue);
    }

    /**
     * Получить значение настройки из БД
     *
     * @param key ключ настройки
     * @return значение из БД
     */
    protected String getProperty(String key) {
        return configsRepository.getConfig(key);
    }
}
