package com.github.tatercertified.testmod;

import com.github.tatercertified.mixin_config.MCMixinConfigPlugin;
import com.github.tatercertified.mixin_config.MixinConfig;
import net.fabricmc.loader.api.FabricLoader;

public class TestModMixinConfigPlugin extends MCMixinConfigPlugin {
    @Override
    public void onLoad(String s) {
        MixinConfig.init(FabricLoader.getInstance().getConfigDir().resolve("test.txt"), 0, Testmod.class);
        super.onLoad(s);
    }
}
