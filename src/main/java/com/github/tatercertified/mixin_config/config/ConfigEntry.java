package com.github.tatercertified.mixin_config.config;

/**
 * Entry of data in the config file
 * @param name Name to call the method/class in the config
 * @param value The current enabled status
 * @param type Either Class or Method
 */
public record ConfigEntry(String name, boolean value, EntryType type) {
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConfigEntry entry) {
            return name.equals(entry.name) && type.equals(entry.type);
        }
        return false;
    }

    /**
     * Gets the written value from the config file
     * @param name Name in the method/class's Annotation and config file
     * @return The status of the method/class Mixin (enabled/disabled)
     */
    public static boolean getConfigValue(String name) {
        for (ContainedConfigEntry containedEntry : ConfigIO.entries) {
            if (containedEntry.classEntry() != null && containedEntry.classEntry().name().equals(name)) {
                return containedEntry.classEntry().value();
            } else {
                for (ConfigEntry entry : containedEntry.methodEntries()) {
                    if (entry.name().equals(name)) {
                        return entry.value();
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gets the written value from the config file
     * @param methodName Name in the method's Annotation and config file
     * @param className Name in the class's Annotation that the method is in
     * @return The status of the method/class Mixin (enabled/disabled)
     */
    public static boolean getConfigValue(String methodName, String className) {
        for (ContainedConfigEntry containedEntry : ConfigIO.entries) {
            if (containedEntry.classEntry() != null && containedEntry.classEntry().name().equals(className)) {
                for (ConfigEntry entry : containedEntry.methodEntries()) {
                    if (entry.name().equals(methodName)) {
                        return entry.value();
                    }
                }
            }
        }
        return getConfigValue(methodName);
    }
}
