package com.github.tatercertified.mixin_config;

import com.github.tatercertified.mixin_config.asm.JarReader;
import com.github.tatercertified.mixin_config.config.ConfigIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class MixinConfig {
    public static final Logger LOGGER = LoggerFactory.getLogger("Mixin Config");

    public static void init(Path config, Path modJar) {
        ConfigIO.setConfigPath(config);
        JarReader.setJarPath(modJar);
    }

    public static void init(Path config, Class<?> yourClass) {
        ConfigIO.setConfigPath(config);
        JarReader.setJarPathFromClass(yourClass);
    }
}