package com.github.tatercertified.mixin_config.validation;

import com.github.tatercertified.mixin_config.MixinConfig;
import com.github.tatercertified.mixin_config.annotations.Config;
import com.github.tatercertified.mixin_config.config.ConfigEntry;
import com.github.tatercertified.mixin_config.config.ConfigIO;
import com.github.tatercertified.mixin_config.config.ContainedConfigEntry;
import com.github.tatercertified.mixin_config.utils.NodeUtils;
import com.github.tatercertified.mixin_config.utils.NotifyingHashSet;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

public final class Validator {
    private static NotifyingHashSet<String> disabled = new NotifyingHashSet<>("Disabled {}");

    /**
     * Generates all necessary data for the processing of Mixins
     */
    public static void preprocess() {
        boolean needsConfig = ConfigIO.needsNewConfig();
        NodeUtils.initNodeUtils();
        if (!needsConfig) {
            ConfigIO.readConfig();
        }
        preprocessAnnotations();
    }

    /**
     * Determines if a Mixin and its methods should be applied
     * @param classNode ASM {@link ClassNode} instance from {@link NodeUtils#getNode(String)}
     * @return True if the Mixin Class should be applied, false if not
     */
    public static boolean process(ClassNode classNode) {
        // Process Class
        AnnotationNode annotationNodeClass = NodeUtils.getAnnotationNode(classNode);
        if (annotationNodeClass != null) {
            Config config = NodeUtils.getInstance(annotationNodeClass);
            if (MixinConfig.VERBOSE) {
                MixinConfig.LOGGER.info("Processing Class {}", config.name());
            }
            if (isDisabled(config.name())) {
                return false;
            }
        }

        // Process Methods
        ListIterator<MethodNode> iterator = classNode.methods.listIterator();
        while (iterator.hasNext()) {
            MethodNode methodNode = iterator.next();
            AnnotationNode annotationNodeMethod = NodeUtils.getAnnotationNode(methodNode);
            if (annotationNodeMethod != null) {
                Config config = NodeUtils.getInstance(annotationNodeMethod);
                if (MixinConfig.VERBOSE) {
                    MixinConfig.LOGGER.info("Processing Method {}", config.name());
                }
                if (isDisabled(config.name())) {
                    iterator.remove();
                }
            }
        }
        return true;
    }

    private static void preprocessAnnotations() {
        for (ContainedConfigEntry containedEntry : ConfigIO.entries) {
            // Class
            if (containedEntry.classEntry() != null && !containedEntry.classEntry().value()) {
                disabled.add(containedEntry.classEntry().name());
                traverseDependencies(containedEntry.classEntry().name());
            }

            // Methods
            for (ConfigEntry entry : containedEntry.methodEntries()) {
                if (!entry.value()) {
                    disabled.add(entry.name());
                    traverseDependencies(entry.name());
                }
            }
        }
    }

    private static void traverseDependencies(String entryName) {
        String[] deps = NodeUtils.getDependencies(entryName);
        if (deps != null) {
            for (String dep : deps) {

                if (disabled.add(dep)) {
                    traverseDependencies(dep);
                }
            }
        }
    }

    /**
     * If the entry is disabled by the config or by a dependency
     * @param entryName Name of the entry: {@link ConfigEntry#name()}
     * @return True if disabled, else false if not or entryName is invalid
     */
    public static boolean isDisabled(String entryName) {
        return disabled.contains(entryName);
    }

    /**
     * Cleans up the static HashSet to free memory
     */
    public static void finish() {
        disabled = null;
    }
}
