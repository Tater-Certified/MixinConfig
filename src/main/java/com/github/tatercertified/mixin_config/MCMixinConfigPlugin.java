package com.github.tatercertified.mixin_config;

import com.github.tatercertified.mixin_config.config.ConfigIO;
import com.github.tatercertified.mixin_config.utils.NodeUtils;
import com.github.tatercertified.mixin_config.validation.Validator;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class MCMixinConfigPlugin implements IMixinConfigPlugin {
    private short mixinCount = 0;
    /**
     * Called after the plugin is instantiated, do any setup here.<p>
     * Ensure that your code is before the super call or that {@link Validator#preprocess()} is called after you call
     * {@link MixinConfig#init(Path, int, Class)}
     * @param s The mixin root package from the config
     */
    @Override
    public void onLoad(String s) {
        Validator.preprocess();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetName, String mixinName) {
        mixinCount++;
        ClassNode node;
        try {
            node = NodeUtils.getNode(mixinName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        boolean result = Validator.process(node);

        // Check if this is the last Mixin
        if (mixinCount == NodeUtils.getMixinCount()) {
            if (MixinConfig.VERBOSE) {
                MixinConfig.LOGGER.info("Cleaning up");
            }
            NodeUtils.finish();
            Validator.finish();
            ConfigIO.finish();
        }
        return result;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }
}
