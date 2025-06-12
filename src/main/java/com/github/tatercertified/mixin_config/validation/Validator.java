package com.github.tatercertified.mixin_config.validation;

import com.github.tatercertified.mixin_config.MixinConfig;
import com.github.tatercertified.mixin_config.annotations.Config;
import com.github.tatercertified.mixin_config.asm.ASMRemover;
import com.github.tatercertified.mixin_config.config.ConfigEntry;
import com.github.tatercertified.mixin_config.config.ConfigIO;
import com.github.tatercertified.mixin_config.config.ContainedConfigEntry;
import com.github.tatercertified.mixin_config.utils.NodeUtils;
import com.github.tatercertified.mixin_config.utils.NotifyingHashSet;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class Validator {
    private static final NotifyingHashSet<String> DISABLED = new NotifyingHashSet<>("Disabled {}");

    /**
     * Generates all necessary data for the processing of Mixins
     */
    public static void preprocess() {
        NodeUtils.initNodeUtils();
        if (!ConfigIO.firstLaunch()) {
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
        for (MethodNode methodNode : classNode.methods) {
            AnnotationNode annotationNodeMethod = NodeUtils.getAnnotationNode(methodNode);
            if (annotationNodeMethod != null) {
                Config config = NodeUtils.getInstance(annotationNodeMethod);
                if (MixinConfig.VERBOSE) {
                    MixinConfig.LOGGER.info("Processing Method {}", config.name());
                }
                if (isDisabled(config.name())) {
                    ASMRemover.removeMethod(classNode, methodNode);
                }
            }
        }
        return true;
    }

    private static void preprocessAnnotations() {
        for (ContainedConfigEntry containedEntry : ConfigIO.ENTRIES) {
            // Class
            if (containedEntry.classEntry() != null && !containedEntry.classEntry().value()) {
                DISABLED.add(containedEntry.classEntry().name());
                traverseDependencies(containedEntry.classEntry().name());
            }

            // Methods
            for (ConfigEntry entry : containedEntry.methodEntries()) {
                if (!entry.value()) {
                    DISABLED.add(entry.name());
                    traverseDependencies(entry.name());
                }
            }
        }
    }

    private static void traverseDependencies(String entryName) {
        String[] deps = NodeUtils.getDependencies(entryName);
        if (deps != null) {
            for (String dep : deps) {
                DISABLED.add(dep);
                traverseDependencies(dep);
            }
        }
    }

    private static boolean isDisabled(String entryName) {
        return DISABLED.contains(entryName);
    }
}
