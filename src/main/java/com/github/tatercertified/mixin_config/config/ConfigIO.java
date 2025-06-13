package com.github.tatercertified.mixin_config.config;

import com.github.tatercertified.mixin_config.MixinConfig;

import java.io.*;
import java.nio.file.Path;
import java.util.LinkedList;

public final class ConfigIO {
    public static LinkedList<ContainedConfigEntry> entries = new LinkedList<>();
    private static Path configPath;
    private static int configVersion;

    /**
     * Writes the config to file
     */
    public static void writeConfig() {
        File config = new File(configPath.toUri());
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(config))) {
            config.createNewFile();
            bw.write("Config Version = " + configVersion);
            bw.newLine();
            bw.newLine();
            ContainedConfigEntry classlessEntry = null;
            for (ContainedConfigEntry containedConfigEntry : entries) {
                if (containedConfigEntry.classEntry() != null) {
                    bw.write(writeLine(containedConfigEntry.classEntry()));
                    bw.newLine();

                    for (ConfigEntry entry : containedConfigEntry.methodEntries()) {
                        bw.write(writeLine(entry));
                        bw.newLine();
                    }
                    bw.newLine();
                } else {
                    classlessEntry = containedConfigEntry;
                }
            }
            if (classlessEntry != null) {
                for (ConfigEntry entry : classlessEntry.methodEntries()) {
                    bw.write(writeLine(entry));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            MixinConfig.LOGGER.warn("Failed to Generate Config, This is Going to Crash and Burn: ", e);
        }
        MixinConfig.LOGGER.info("Generated Config");
    }

    private static String writeLine(ConfigEntry entry) {
        if (entry.type() == EntryType.Class) {
            return "[ " + entry.name() + " ] = " + entry.value();
        } else {
            return "  " + entry.name() + " = " + entry.value();
        }
    }

    /**
     * Parses the data from the config
     */
    public static void readConfig() {
        File config = new File(configPath.toUri());
        if (config.exists()) {
            parse(config);
        }
    }

    /**
     * If the config needs created
     * @return True if this is the first launch or the config file is missing/outdated, else false
     */
    public static boolean needsNewConfig() {
        File config = new File(configPath.toUri());
        return !config.exists() || parseVersion(config) != configVersion;
    }

    /**
     * Sets the config path variable
     * @param path Path to set it to including the extension (.txt)
     */
    public static void setConfigPath(Path path) {
        configPath = path;
    }

    /**
     * Sets the config version variable
     * @param version Integer version of the config file
     */
    public static void setConfigVersion(int version) {
        configVersion = version;
    }

    private static void parse(File config) {
        ConfigEntry clazz = null;
        LinkedList<ConfigEntry> methods = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(config))) {
            String line;
            int index = 0;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    Object[] values = parseLine(line, index);

                    if (values != null) {
                        if (values[2] == EntryType.Class) {
                            clazz = new ConfigEntry((String) values[0], (Boolean) values[1], EntryType.Class);
                        } else {
                            methods.add(new ConfigEntry((String) values[0], (Boolean) values[1], EntryType.Method));
                        }
                    }
                } else {
                    entries.add(new ContainedConfigEntry(clazz, new LinkedList<>(methods)));
                    clazz = null;
                    methods.clear();
                }
                index++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!methods.isEmpty() || clazz != null) {
            entries.add(new ContainedConfigEntry(clazz, methods));
        }
    }

    private static int parseVersion(File config) {
        try (BufferedReader br = new BufferedReader(new FileReader(config))) {
            String versionLine = br.readLine();
            String[] parts = versionLine.split("=");
            return Integer.parseInt(parts[1].strip());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object[] parseLine(String line, int index) {
        if (index != 0) {
            Object[] results = new Object[3];
            String[] parts = line.split("=");
            results[2] = parts[0].contains("[") ? EntryType.Class : EntryType.Method;
            results[0] = parts[0].replace("[", "").replace("]", "").strip();
            results[1] = Boolean.parseBoolean(parts[1].strip());
            return results;
        } else {
            return null;
        }
    }

    /**
     * Cleans the static List to free memory
     */
    public static void finish() {
        entries = null;
    }
}
