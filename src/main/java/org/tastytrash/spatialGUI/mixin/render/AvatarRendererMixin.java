package org.tastytrash.spatialGUI.mixin.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
    @Unique private static float smoothedHeadYaw = 0f;
    @Unique private static float smoothedHeadPitch = 0f;

    @Unique private static final float MAX_YAW_OFFSET = 40f;
    @Unique private static final float MAX_PITCH_OFFSET = 25f;
    @Unique private static final float SMOOTHING = 0.15f;

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("TAIL")
    )
    private void diegeticInventory$overrideHeadLook(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        var renderer = SpatialGUIClient.renderer();

        if (entity != client.player || !renderer.shouldCapture() || !SpatialGUI.config.enabled) {
            renderer.headLockInitialized = false;
            return;
        }

        boolean justOpened = !renderer.headLockInitialized;

        double mx = client.mouseHandler.xpos();
        double my = client.mouseHandler.ypos();
        int w = client.getWindow().getScreenWidth();
        int h = client.getWindow().getScreenHeight();

        float nx = (float) (mx / w) * 2f - 1f;
        float ny = (float) (my / h) * 2f - 1f;

        state.bodyRot = client.player.getYRot() + (float) SpatialGUI.config.avatarBodyRotationOffset;

        float targetYaw = nx * MAX_YAW_OFFSET + (float) SpatialGUI.config.avatarBaseYawOffset;
        float targetPitch = Mth.clamp(ny * MAX_PITCH_OFFSET, -60f, 60f);

        if (justOpened) {
            smoothedHeadYaw = targetYaw;
            smoothedHeadPitch = targetPitch;
            renderer.headLockInitialized = true;
        } else {
            smoothedHeadYaw = Mth.rotLerp(SMOOTHING, smoothedHeadYaw, targetYaw);
            smoothedHeadPitch = Mth.lerp(SMOOTHING, smoothedHeadPitch, targetPitch);
        }

        state.yRot = smoothedHeadYaw;
        state.xRot = smoothedHeadPitch;
    }
}