package org.tastytrash.spatialGUI.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;

//? if >=26.1.2 {
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
//?} else {
/*@Mixin(Minecraft.class)
*///?}
public class MinecraftMixin {
    // setScreenAndShow forces a frame without the level rendered, which would flash black
    // behind the screen, so render it
    //? if >=26.1.2 {
    @ModifyArg(
            method = "setScreenAndShow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;renderFrame(Z)V"),
            index = 0
    )
    private boolean spatialGUI$renderWorldInForcedFrame(boolean original) {
        var screen = SpatialGUIRenderer.currentScreen();
        if (original || screen == null || Minecraft.getInstance().level == null
                || ConfigManager.config() == null || !ConfigManager.config().enabled) {
            return original;
        }
        String id = screen.getClass().getName();
        return !id.contains("LevelLoading") && !id.contains("ReceivingLevel");
    }

    @Inject(method = "pick", at = @At("TAIL"))
    private void spatialGUI$clearTargetWhileInGui(float partialTicks, CallbackInfo ci) {
        SpatialGUIClient.clearTarget();
    }
    //?}

    @Inject(method = "grabPanoramixScreenshot", at = @At("HEAD"))
    private void spatialGUI$onPanoramaCaptureStart(CallbackInfoReturnable<String> cir) {
        SpatialGUIClient.setCapturingPanorama(true);
    }

    @Inject(method = "grabPanoramixScreenshot", at = @At("RETURN"))
    private void spatialGUI$onPanoramaCaptureEnd(CallbackInfoReturnable<String> cir) {
        SpatialGUIClient.setCapturingPanorama(false);
    }
}
