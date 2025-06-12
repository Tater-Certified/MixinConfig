package com.github.tatercertified.mixin_config.config;

import java.io.*;
import java.nio.file.Path;
import java.util.LinkedList;

public final class ConfigIO {
    public static final LinkedList<ContainedConfigEntry> ENTRIES = new LinkedList<>();
    private static Path CONFIG_PATH;

    /**
     * Writes the config to file
     */
    public static void writeConfig() {
        File config = new File(CONFIG_PATH.toUri());
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(config))) {
            for (ContainedConfigEntry containedConfigEntry : ENTRIES) {
                bw.write(writeLine(containedConfigEntry.classEntry()));
                bw.newLine();
                for (ConfigEntry entry : containedConfigEntry.methodEntries()) {
                    bw.write(writeLine(entry));
                    bw.newLine();
                }
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses the data from the config
     */
    public static void readConfig() {
        File config = new File(CONFIG_PATH.toUri());
        if (config.exists()) {
            parse(config);
        }
    }

    /**
     * If the config needs created
     * @return True if this is the first launch or the config file is missing, else false
     */
    public static boolean firstLaunch() {
        File config = new File(CONFIG_PATH.toUri());
        return !config.exists();
    }

    /**
     * Sets the config path variable
     * @param path Path to set it to excluding the extension (.txt)
     */
    public static void setConfigPath(String path) {
        CONFIG_PATH = Path.of(path + ".txt");
    }

    private static void parse(File config) {
        ConfigEntry clazz = null;
        LinkedList<ConfigEntry> methods = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(config))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    Object[] values = parseLine(line);

                    if (values[2] == EntryType.Class) {
                        clazz = new ConfigEntry((String) values[0], (Boolean) values[1], EntryType.Class);
                    } else {
                        methods.add(new ConfigEntry((String) values[0], (Boolean) values[1], EntryType.Method));
                    }
                } else {
                    ENTRIES.add(new ContainedConfigEntry(clazz, new LinkedList<>(methods)));
                    clazz = null;
                    methods.clear();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!methods.isEmpty() || clazz != null) {
            ENTRIES.add(new ContainedConfigEntry(clazz, methods));
        }
    }

    private static Object[] parseLine(String line) {
        Object[] results = new Object[3];
        String[] parts = line.split("=");
        results[2] = parts[0].contains("[") ? EntryType.Class : EntryType.Method;
        results[0] = parts[0].replace("[", "").replace("]", "").strip();
        results[1] = Boolean.parseBoolean(parts[1].strip());
        return results;
    }

    private static String writeLine(ConfigEntry entry) {
        if (entry.type() == EntryType.Class) {
            return "[ " + entry.name() + "] = " + entry.value();
        } else {
            return " " + entry.name() + " = " + entry.value();
        }
    }
}
