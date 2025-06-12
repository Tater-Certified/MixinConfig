package com.github.tatercertified.mixin_config.utils;

import com.github.tatercertified.mixin_config.MixinConfig;
import com.github.tatercertified.mixin_config.annotations.Config;
import com.github.tatercertified.mixin_config.asm.JarReader;
import com.github.tatercertified.mixin_config.config.ConfigEntry;
import com.github.tatercertified.mixin_config.config.ConfigIO;
import com.github.tatercertified.mixin_config.config.ContainedConfigEntry;
import com.github.tatercertified.mixin_config.config.EntryType;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public final class NodeUtils {
    private static final HashMap<String, String[]> CACHED_DEPENDENCIES = new HashMap<>();

    /**
     * Gets the {@link AnnotationNode} for a {@link ClassNode}
     * @param node ClassNode instance
     * @return AnnotationNode for the {@link Config} Annotation, or null if not present
     */
    @Nullable
    public static AnnotationNode getAnnotationNode(ClassNode node) {
        for (AnnotationNode annotationNode : node.visibleAnnotations) {
            if (isAnnotation(annotationNode.desc)) {
                return annotationNode;
            }
        }
        return null;
    }

    /**
     * Gets the {@link AnnotationNode} for a {@link MethodNode}
     * @param node MethodNode instance
     * @return AnnotationNode for the {@link Config} Annotation, or null if not present
     */
    @Nullable
    public static AnnotationNode getAnnotationNode(MethodNode node) {
        for (AnnotationNode annotationNode : node.visibleAnnotations) {
            if (isAnnotation(annotationNode.desc)) {
                return annotationNode;
            }
        }
        return null;
    }

    /**
     * If the current Annotation is {@link Config}
     * @param desc AnnotationNode's description: {@link AnnotationNode#desc}
     * @return True if the Annotation matches, else false
     */
    public static boolean isAnnotation(String desc) {
        return desc.equals(Config.class.descriptorString());
    }

    /**
     * Gets a {@link Config} instance from an {@link AnnotationNode}
     * @param node AnnotationNode instance
     * @return Config that contains all the data in the AnnotationNode
     */
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

    /**
     * Assembles the dependency tree and creates a default config on {@link ConfigIO#firstLaunch()}
     */
    public static void initNodeUtils() {
        boolean firstLaunch = ConfigIO.firstLaunch();
        List<ClassNode> classNodes;
        try {
            classNodes = JarReader.getClassNodes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (ClassNode classNode : classNodes) {
            List<String> classDependencies = new ArrayList<>();
            ConfigEntry classEntry = null; // First launch
            String classConfigName = null;
            AnnotationNode annotationNodeClass = NodeUtils.getAnnotationNode(classNode);
            if (annotationNodeClass != null) {
                Config config = NodeUtils.getInstance(annotationNodeClass);
                if (firstLaunch) {
                    classEntry = configToEntry(config, EntryType.Class);
                }
                classConfigName = config.name();
                if (config.dependencies().length != 0) {
                    classDependencies.addAll(List.of(config.dependencies()));
                }
            }

            LinkedList<ConfigEntry> methodEntries = new LinkedList<>(); // First launch
            for (MethodNode methodNode : classNode.methods) {
                AnnotationNode annotationNodeMethod = NodeUtils.getAnnotationNode(methodNode);
                if (annotationNodeMethod != null) {
                    Config config = NodeUtils.getInstance(annotationNodeMethod);
                    if (firstLaunch) {
                        methodEntries.add(configToEntry(config, EntryType.Method));
                    }
                    classDependencies.add(config.name());
                    if (config.dependencies().length != 0) {
                        CACHED_DEPENDENCIES.put(config.name(), config.dependencies());
                    }
                }
            }

            // Gather data for first launch
            if (firstLaunch && (!methodEntries.isEmpty() || classEntry != null)) {
                if (classEntry == null) {
                    // Combine classless methods
                    boolean combined = false;
                    for (ContainedConfigEntry containedEntry : ConfigIO.ENTRIES) {
                        if (containedEntry.classEntry() == null) {
                            containedEntry.methodEntries().addAll(methodEntries);
                            combined = true;
                            break;
                        }
                    }
                    // Make new classless contained config entry
                    if (!combined) {
                        ConfigIO.ENTRIES.add(new ContainedConfigEntry(null, methodEntries));
                    }
                } else {
                    ConfigIO.ENTRIES.add(new ContainedConfigEntry(classEntry, methodEntries));
                }

                // Write / Parse Config
                ConfigIO.writeConfig();
            }

            if (classConfigName != null) {
                CACHED_DEPENDENCIES.put(classConfigName, classDependencies.toArray(new String[0]));
            }
        }
    }

    /**
     * Gets all immediate dependencies of a {@link ConfigEntry}
     * @param name Name of the ConfigEntry: {@link ConfigEntry#name()}
     * @return Array of all ConfigEntry names that are immediate dependents
     */
    @Nullable
    public static String[] getDependencies(String name) {
        return CACHED_DEPENDENCIES.get(name);
    }

    /**
     * Gets a ClassNode from a class's dot path (com.example.Class)
     * @param classPath Class's dot path
     * @return ASM {@link ClassNode} for the class
     * @throws ClassNotFoundException Class couldn't be found
     */
    public static ClassNode getNode(String classPath) throws ClassNotFoundException {
        String resourcePath = classPath.replace('.', '/') + ".class";

        InputStream is = ClassLoader.getSystemResourceAsStream(resourcePath);
        if (is == null) {
            MixinConfig.LOGGER.error("Class not found from class path: {}", classPath);
            throw new ClassNotFoundException();
        }

        ClassReader cr;
        try {
            cr = new ClassReader(is);
        } catch (IOException e) {
            MixinConfig.LOGGER.error("Failed to read class: {}", classPath);
            throw new ClassNotFoundException();
        }
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        return cn;
    }

    private static ConfigEntry configToEntry(Config config, EntryType type) {
        return new ConfigEntry(config.name(), config.defaultVal(), type);
    }
}
