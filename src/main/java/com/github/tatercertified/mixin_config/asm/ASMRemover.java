package com.github.tatercertified.mixin_config.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class ASMRemover {
    public static void removeMethod(ClassNode classNode, MethodNode methodNode) {
        classNode.methods.remove(methodNode);
        // TODO See if more wild ASM is required
    }
}
