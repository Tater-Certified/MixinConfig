package com.github.tatercertified.testmod.mixin;

import com.github.tatercertified.testmod.Testmod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class FinalMixin {
    private boolean needsToRun = true;

    @Inject(method = "tick", at = @At("HEAD"))
    private void testmod$tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (needsToRun) {
            MathHelper.sin(0.1F);
            MathHelper.cos(0.1F);
            Testmod.runTests();
            needsToRun = false;
        }
    }
}
