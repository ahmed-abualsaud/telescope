
package org.telescope.config;

import java.util.List;

public class ConfigSuffix<T> {

    private final String keySuffix;
    private final List<KeyType> types;
    private final T defaultValue;

    ConfigSuffix(String keySuffix, List<KeyType> types) {
        this(keySuffix, types,  null);
    }

    ConfigSuffix(String keySuffix, List<KeyType> types, T defaultValue) {
        this.keySuffix = keySuffix;
        this.types = types;
        this.defaultValue = defaultValue;
    }

    public ConfigKey<T> withPrefix(String prefix) {
        return new ConfigKey<>(prefix + keySuffix, types, defaultValue);
    }

}
