package ru.avdonin.template.constatns;


public enum Constants {
    COMPRESSION_AVATAR(32, Integer.class),
    COMPRESSION_IMAGES(230, Integer.class);

    private final Object value;
    private final Class<?> aClass;

    Constants(Object value, Class<?> aClass) {
        this.value = value;
        this.aClass = aClass;
    }

    public <V> V getValue() {
        return (V) aClass.cast(value);
    }
}
