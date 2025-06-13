package com.github.tatercertified.mixin_config.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class JarReader {
    private static Path jarPath;

    /**
     * Sets the path to the jar file
     * @param path Path to the jar file
     */
    public static void setJarPath(Path path) {
        jarPath = path;
    }

    /**
     * Sets the jar file path from a Class in your project
     * @param clazz Class from your project
     */
    public static void setJarPathFromClass(Class<?> clazz) {
        try {
            jarPath = Paths.get(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets all {@link ClassNode}s from your project
     * @return List of all ClassNodes in your project
     * @throws IOException Failed to read the jar
     */
    public static List<ClassNode> getClassNodes() throws IOException {
        List<ClassNode> nodes = new ArrayList<>();
        if (jarPath != null) {
            try (JarFile jarFile = new JarFile(jarPath.toString())) {
                Enumeration<JarEntry> entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        try (InputStream is = jarFile.getInputStream(entry)) {
                            ClassReader cr = new ClassReader(is);
                            ClassNode cn = new ClassNode();
                            cr.accept(cn, 0);
                            nodes.add(cn);
                        }
                    }
                }
            } catch (IOException ignored) {
                try (Stream<Path> stream = Files.walk(jarPath)) {
                    stream.filter(path -> path.toString().endsWith(".class"))
                            .forEach(path -> {
                                try (InputStream is = new FileInputStream(path.toFile())) {
                                    ClassReader cr = new ClassReader(is);
                                    ClassNode cn = new ClassNode();
                                    cr.accept(cn, 0);
                                    nodes.add(cn);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }
            }
        }
        return nodes;
    }
}
