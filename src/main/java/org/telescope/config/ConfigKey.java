
package org.telescope.config;

import java.util.List;

public class ConfigKey<T> {

    private final String key;
    private final List<KeyType> types;
    private final T defaultValue;

    ConfigKey(String key, List<KeyType> types) {
        this(key, types, null);
    }

    ConfigKey(String key, List<KeyType> types, T defaultValue) {
        this.key = key;
        this.types = types;
        this.defaultValue = defaultValue;
    }

    String getKey() {
        return key;
    }

    public List<KeyType> getTypes() {
        return types;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

}
