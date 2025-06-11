package com.github.tatercertified.mixin_config.config;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.ListIterator;

public final class ConfigIO {
    public static final LinkedList<ConfigEntry> ENTRIES = new LinkedList<>();
    private static Path CONFIG_PATH;

    public static void overwriteValue(String name, boolean val) {
        ListIterator<ConfigEntry> iterator = ENTRIES.listIterator();
        while (iterator.hasNext()) {
            ConfigEntry entry = iterator.next();
            if (entry.name().equals(name)) {
                iterator.set(new ConfigEntry(name, val));
                return;
            }
        }
        ENTRIES.add(new ConfigEntry(name, val));
    }

    public static void writeConfig() {

    }

    public static void readConfig() {

    }

    public static void firstLaunch() {

    }

    public static void setConfigPath(Path path) {
        CONFIG_PATH = path;
    }
}
