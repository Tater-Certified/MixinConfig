package com.github.tatercertified.testmod.mixin;

import com.github.tatercertified.mixin_config.annotations.Config;
import com.github.tatercertified.testmod.Testmod;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MathHelper.class)
public class OtherMixin {
    @Config(name = "Sin", dependencies = "Cos")
    @Inject(method = "sin", at = @At("HEAD"))
    private static void testmod$sin(float value, CallbackInfoReturnable<Float> cir) {
        Testmod.FIRED.add("sin");
    }

    @Config(name = "Cos", dependencies = "Sin")
    @Inject(method = "cos", at = @At("HEAD"))
    private static void testmod$cos(float value, CallbackInfoReturnable<Float> cir) {
        Testmod.FIRED.add("cos");
    }
}
