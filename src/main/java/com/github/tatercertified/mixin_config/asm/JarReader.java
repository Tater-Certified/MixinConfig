package com.github.tatercertified.mixin_config.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class JarReader {
    private static Path jarPath;
    public static void setJarPath(Path path) {
        jarPath = path;
    }

    public static void setJarPathFromClass(Class<?> clazz) {
        try {
            jarPath = Paths.get(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ClassNode> getClassNodes() throws IOException {
        List<ClassNode> nodes = new ArrayList<>();
        if (jarPath != null) {
            JarFile jarFile = new JarFile(jarPath.toString());
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

            jarFile.close();
        }
        return nodes;
    }
}
