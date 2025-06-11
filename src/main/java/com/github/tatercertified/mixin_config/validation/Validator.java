package com.github.tatercertified.mixin_config.validation;

import com.github.tatercertified.mixin_config.annotations.Config;
import com.github.tatercertified.mixin_config.asm.ASMRemover;
import com.github.tatercertified.mixin_config.config.ConfigEntry;
import com.github.tatercertified.mixin_config.config.ConfigIO;
import com.github.tatercertified.mixin_config.utils.NodeUtils;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashSet;

public final class Validator {
    private static final HashSet<String> DISABLED = new HashSet<>();
    public static void preprocess() {
        NodeUtils.cacheDependencies();
        preprocessAnnotations();
    }

    public static boolean process(ClassNode classNode) {
        // Process Class
        AnnotationNode annotationNodeClass = NodeUtils.getAnnotationNode(classNode);
        if (annotationNodeClass != null) {
            Config config = NodeUtils.getInstance(annotationNodeClass);
            if (isDisabled(config.name())) {
                return false;
            }
        }

        // Process Methods
        for (MethodNode methodNode : classNode.methods) {
            AnnotationNode annotationNodeMethod = NodeUtils.getAnnotationNode(methodNode);
            if (annotationNodeMethod != null) {
                Config config = NodeUtils.getInstance(annotationNodeMethod);
                if (isDisabled(config.name())) {
                    ASMRemover.removeMethod(classNode, methodNode);
                }
            }
        }
        return true;
    }

    private static void preprocessAnnotations() {
        for (ConfigEntry entry : ConfigIO.ENTRIES) {
            if (!entry.value()) {
                DISABLED.add(entry.name());
                traverseDependencies(entry.name());
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
