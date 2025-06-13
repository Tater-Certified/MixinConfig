package com.github.tatercertified.testmod.mixin;

import com.github.tatercertified.mixin_config.annotations.Config;
import com.github.tatercertified.testmod.Testmod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Config(name = "Startup Mixin")
@Mixin(MinecraftServer.class)
public class StartupMixin {
    @Config(name = "Start Server")
    @Inject(method = "startServer", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;start()V"))
    private static <S> void testmod$startServer(Function<Thread, S> serverFactory, CallbackInfoReturnable<S> cir) {
        Testmod.FIRED.add("startServer");
    }

    @Config(name = "Load World", dependencies = {"Run Server", "Sin"}, enabled = false)
    @Inject(method = "loadWorld", at = @At("TAIL"))
    private void testmod$loadWorld(CallbackInfo ci) {
        Testmod.FIRED.add("loadWorld");
    }

    @Config(name = "Run Server")
    @Inject(method = "runServer", at = @At("HEAD"))
    private void testmod$runServer(CallbackInfo ci) {
        Testmod.FIRED.add("runServer");
    }
}
