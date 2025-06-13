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
import org.spongepowered.asm.mixin.Mixin;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.*;

public final class NodeUtils {
    private static HashMap<String, String[]> cachedDependencies = new HashMap<>();
    private static short mixinCount = 0;

    /**
     * Gets the {@link AnnotationNode} for a {@link ClassNode}
     * @param node ClassNode instance
     * @return AnnotationNode for the {@link Config} Annotation, or null if not present
     */
    @Nullable
    public static AnnotationNode getAnnotationNode(ClassNode node) {
        if (node.visibleAnnotations != null) {
            for (AnnotationNode annotationNode : node.visibleAnnotations) {
                if (isAnnotation(annotationNode.desc)) {
                    return annotationNode;
                }
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
        if (node.visibleAnnotations != null) {
            for (AnnotationNode annotationNode : node.visibleAnnotations) {
                if (isAnnotation(annotationNode.desc)) {
                    return annotationNode;
                }
            }
        }
        return null;
    }

    /**
     * Checks if the current class is a {@link Mixin}
     * @param node {@link ClassNode} instance
     * @return True if the class is a Mixin, else false
     */
    public static boolean checkForMixin(ClassNode node) {
        if (node.invisibleAnnotations != null) {
            for (AnnotationNode annotationNode : node.invisibleAnnotations) {
                if (isMixin(annotationNode.desc)) {
                    mixinCount++;
                    return true;
                }
            }
        }
        return false;
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
     * If the current Annotation is {@link Mixin}
     * @param desc AnnotationNode's description: {@link AnnotationNode#desc}
     * @return True if the Annotation matches, else false
     */
    public static boolean isMixin(String desc) {
        return desc.equals(Mixin.class.descriptorString());
    }

    /**
     * The number of Mixins loaded in the project
     * @return The number of loaded Mixins (applied and unapplied)
     */
    public static short getMixinCount() {
        return mixinCount;
    }

    /**
     * Gets a {@link Config} instance from an {@link AnnotationNode}
     * @param node AnnotationNode instance
     * @return Config that contains all the data in the AnnotationNode
     */
    public static Config getInstance(AnnotationNode node) {
        Map<String, Object> annotationValues = new HashMap<>();

        for (int i = 0; i < node.values.size(); i += 2) {
            String k = (String) node.values.get(i);
            Object v = node.values.get(i + 1);
            annotationValues.put(k, v);
        }

        String entryName = (String) annotationValues.getOrDefault("name", "");
        boolean enabled = (boolean) annotationValues.getOrDefault("enabled", true);
        Object depsPreCast = annotationValues.getOrDefault("dependencies", new String[0]);
        String[] dependencies;
        if (depsPreCast instanceof String[] arr) {
            dependencies = arr;
        } else {
            dependencies = ((ArrayList<String>) depsPreCast).toArray(new String[0]);
        }

        return new Config() {
            @Override public String name() { return entryName; }
            @Override public boolean enabled() { return enabled; }
            @Override public String[] dependencies() { return dependencies; }
            @Override public Class<? extends Annotation> annotationType() { return Config.class; }
        };
    }

    /**
     * Assembles the dependency tree and creates a default config on {@link ConfigIO#needsNewConfig()}
     */
    public static void initNodeUtils() {
        boolean firstLaunch = ConfigIO.needsNewConfig();
        List<ClassNode> classNodes;
        try {
            classNodes = JarReader.getClassNodes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (ClassNode classNode : classNodes) {
            // Check if the class is even a Mixin
            if (checkForMixin(classNode)) {
                if (MixinConfig.VERBOSE) {
                    MixinConfig.LOGGER.info("Checking {}", classNode.name);
                }
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
                            cachedDependencies.put(config.name(), config.dependencies());
                        }
                    }
                }

                // Gather data for first launch
                if (firstLaunch && (!methodEntries.isEmpty() || classEntry != null)) {
                    if (classEntry == null) {
                        // Combine classless methods
                        boolean combined = false;
                        for (ContainedConfigEntry containedEntry : ConfigIO.entries) {
                            if (containedEntry.classEntry() == null) {
                                containedEntry.methodEntries().addAll(methodEntries);
                                combined = true;
                                break;
                            }
                        }
                        // Make new classless contained config entry
                        if (!combined) {
                            ConfigIO.entries.add(new ContainedConfigEntry(null, methodEntries));
                        }
                    } else {
                        ConfigIO.entries.add(new ContainedConfigEntry(classEntry, methodEntries));
                    }
                }

                if (classConfigName != null) {
                    cachedDependencies.put(classConfigName, classDependencies.toArray(new String[0]));
                }
            }
        }

        if (firstLaunch) {
            ConfigIO.writeConfig();
        }
    }

    /**
     * Gets all immediate dependencies of a {@link ConfigEntry}
     * @param name Name of the ConfigEntry: {@link ConfigEntry#name()}
     * @return Array of all ConfigEntry names that are immediate dependents
     */
    @Nullable
    public static String[] getDependencies(String name) {
        return cachedDependencies.get(name);
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
        return new ConfigEntry(config.name(), config.enabled(), type);
    }

    /**
     * Cleans the static HashMap to free memory
     */
    public static void finish() {
        cachedDependencies = null;
    }
}
