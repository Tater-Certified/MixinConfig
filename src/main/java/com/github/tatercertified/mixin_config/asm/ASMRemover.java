package com.github.tatercertified.mixin_config.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class ASMRemover {
    /**
     * Removes a method from a Mixin
     * @param classNode ASM {@link ClassNode} to remove the method from
     * @param methodNode ASM {@link MethodNode} to be removed
     */
    public static void removeMethod(ClassNode classNode, MethodNode methodNode) {
        classNode.methods.remove(methodNode);
        // TODO See if more wild ASM is required
    }
}
