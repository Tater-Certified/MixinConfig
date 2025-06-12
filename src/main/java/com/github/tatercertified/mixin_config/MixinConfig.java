package com.github.tatercertified.mixin_config;

import com.github.tatercertified.mixin_config.asm.JarReader;
import com.github.tatercertified.mixin_config.config.ConfigIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class MixinConfig {
    /**
     * The {@link Logger} for MixinConfig
     */
    public static final Logger LOGGER = LoggerFactory.getLogger("Mixin Config");

    /**
     * If more debugging should be enabled: -Dmixinconfig.verbose=true/false
     */
    public static final boolean VERBOSE = "true".equals(System.getProperty("mixinconfig.verbose"));

    /**
     * Called on startup by your code.<p>
     * Alternatively, you can use {@link MixinConfig#init(String, Class)}
     * @param config Path to the config file excluding the extension (.txt)
     * @param modJar Path to the mod/plugin jar file
     */
    public static void init(String config, Path modJar) {
        ConfigIO.setConfigPath(config);
        JarReader.setJarPath(modJar);
    }

    /**
     * Called on startup by your code.<p>
     * Alternatively, you can use {@link MixinConfig#init(String, Path)}
     * @param config Path to the config file excluding the extension (.txt)
     * @param yourClass A class in your project
     */
    public static void init(String config, Class<?> yourClass) {
        ConfigIO.setConfigPath(config);
        JarReader.setJarPathFromClass(yourClass);
    }
}