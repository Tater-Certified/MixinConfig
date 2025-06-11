package com.github.tatercertified.mixin_config.utils;

import com.github.tatercertified.mixin_config.annotations.Config;
import com.github.tatercertified.mixin_config.asm.JarReader;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class NodeUtils {
    private static final HashMap<String, String[]> CACHED_DEPENDENCIES = new HashMap<>();

    @Nullable
    public static AnnotationNode getAnnotationNode(ClassNode node) {
        for (AnnotationNode annotationNode : node.visibleAnnotations) {
            if (isAnnotation(annotationNode.desc)) {
                return annotationNode;
            }
        }
        return null;
    }

    @Nullable
    public static AnnotationNode getAnnotationNode(MethodNode node) {
        for (AnnotationNode annotationNode : node.visibleAnnotations) {
            if (isAnnotation(annotationNode.desc)) {
                return annotationNode;
            }
        }
        return null;
    }

    public static boolean isAnnotation(String desc) {
        return desc.equals(Config.class.descriptorString());
    }

    public static Config getInstance(AnnotationNode node) {
        String entryName = "";
        boolean defaultValue = false;
        String[] dependencies = {};
        for (Object param : node.values) {
            if (param instanceof String name) {
                entryName = name;
            } else if (param instanceof String[] deps) {
                dependencies = deps;
            } else {
                defaultValue = (boolean) param;
            }
        }
        boolean finalDefaultValue = defaultValue;
        String finalEntryName = entryName;
        String[] finalDependencies = dependencies;
        return new Config() {
            @Override
            public String name() {
                return finalEntryName;
            }

            @Override
            public boolean defaultVal() {
                return finalDefaultValue;
            }

            @Override
            public String[] dependencies() {
                return finalDependencies;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Config.class;
            }
        };
    }

    public static void cacheDependencies() {
        List<ClassNode> classNodes;
        try {
            classNodes = JarReader.getClassNodes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (ClassNode classNode : classNodes) {
            List<String> classDependencies = new ArrayList<>();
            String classConfigName = null;
            AnnotationNode annotationNodeClass = NodeUtils.getAnnotationNode(classNode);
            if (annotationNodeClass != null) {
                Config config = NodeUtils.getInstance(annotationNodeClass);
                classConfigName = config.name();
                if (config.dependencies().length != 0) {
                    classDependencies.addAll(List.of(config.dependencies()));
                }
            }

            for (MethodNode methodNode : classNode.methods) {
                AnnotationNode annotationNodeMethod = NodeUtils.getAnnotationNode(methodNode);
                if (annotationNodeMethod != null) {
                    Config config = NodeUtils.getInstance(annotationNodeMethod);
                    classDependencies.add(config.name());
                    if (config.dependencies().length != 0) {
                        CACHED_DEPENDENCIES.put(config.name(), config.dependencies());
                    }
                }
            }
            if (classConfigName != null) {
                CACHED_DEPENDENCIES.put(classConfigName, classDependencies.toArray(new String[0]));
            }
        }
    }

    @Nullable
    public static String[] getDependencies(String name) {
        return CACHED_DEPENDENCIES.get(name);
    }
}
