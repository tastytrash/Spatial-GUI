package org.tastytrash.spatialGUI.mixin.player;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Shadow public float xBob;
    @Shadow public float yBob;
    @Shadow public float xBobO;
    @Shadow public float yBobO;

    @Shadow protected abstract boolean isControlledCamera();

    @Unique
    private static boolean spatialGUI$swayActive() {
        var renderer = SpatialGUIClient.renderer();
        return renderer != null && renderer.bobSwayActive();
    }

    //? if >1.21.1 {
    @Inject(method = "applyInput", at = @At("TAIL"))
    //?} else {
    /*@Inject(method = "serverAiStep", at = @At("TAIL"))
    *///?}
    private void spatialGUI$bobFollowsCamera(CallbackInfo ci) {
        if (!spatialGUI$swayActive() || !this.isControlledCamera()) return;

        this.xBob = this.xBobO + (SpatialGUIRenderer.cameraPitch() - this.xBobO) * 0.5F;
        this.yBob = this.yBobO + Mth.wrapDegrees(SpatialGUIRenderer.cameraYaw() - this.yBobO) * 0.5F;
    }

    //? if <26.3 {
    @ModifyReturnValue(method = "getViewXRot", at = @At("RETURN"))
    private float spatialGUI$cameraViewXRot(float original, float a) {
        if (!spatialGUI$swayActive()) return original;
        return SpatialGUIRenderer.cameraPitch();
    }
    //?}

    @ModifyReturnValue(method = "getViewYRot", at = @At("RETURN"))
    private float spatialGUI$cameraViewYRot(float original, float a) {
        if (!spatialGUI$swayActive()) return original;
        float bob = Mth.lerp(a, this.yBobO, this.yBob);
        return bob + Mth.wrapDegrees(SpatialGUIRenderer.cameraYaw() - bob);
    }
}
