package org.tastytrash.spatialGUI.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.compat.ShaderCompat;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;
import org.tastytrash.spatialGUI.render.WorldBlurRenderer;

//? if >1.21.1 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
//?} else {
/*import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
*///?}

//? if >=26.1.2 {
import net.minecraft.client.renderer.state.level.CameraRenderState;
//?} else if 1.21.11 {
/*import net.minecraft.client.renderer.state.CameraRenderState;
 *///?}

@Mixin(value = EntityRenderDispatcher.class, priority = 500)
public class EntityRenderDispatcherMixin {
    //? if 1.21.11 {
    /*@Unique
    private static EntityRenderState spatialGUI$localPlayerState;

    @Inject(method = "extractEntity", at = @At("TAIL"))
    private void spatialGUI$trackLocalPlayerState(Entity entity, float partialTick, CallbackInfoReturnable<EntityRenderState> cir) {
        if (entity == Minecraft.getInstance().player && !SpatialGUIRenderer.isExtractingScreen) {
            spatialGUI$localPlayerState = cir.getReturnValue();
        }
    }

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$deferLocalPlayer(EntityRenderState renderState, CameraRenderState camera, double x, double y, double z, PoseStack poseStack, SubmitNodeCollector output, CallbackInfo ci) {
        var renderer = SpatialGUIClient.renderer();
        if (renderState != spatialGUI$localPlayerState || !SpatialGUIRenderer.renderingLevel || WorldBlurRenderer.drawingDeferredPlayer || renderer == null || !renderer.shouldDeferPlayer()) {
            return;
        }

        WorldBlurRenderer.deferredPlayerState = renderState;
        WorldBlurRenderer.deferredPlayerCamera = camera;
        WorldBlurRenderer.deferredPlayerX = x;
        WorldBlurRenderer.deferredPlayerY = y;
        WorldBlurRenderer.deferredPlayerZ = z;
        ci.cancel();
    }
    *///?}

    //? if <=1.21.1 {
    /*@Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void spatialGUI$deferLocalPlayer(E entity, double x, double y, double z, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        var renderer = SpatialGUIClient.renderer();
        if (entity != Minecraft.getInstance().player || !SpatialGUIRenderer.renderingLevel || renderer == null
                || renderer.getWorldScreenRenderer().isDrawingDeferredPlayer() || !renderer.shouldDeferPlayer()) {
            return;
        }

        renderer.getWorldScreenRenderer().deferPlayer((AbstractClientPlayer) entity, x, y, z, yaw, partialTick, packedLight);
        ci.cancel();
    }
    *///?}

    //? if >=26.1.2 {
    @Inject(method = "extractEntity", at = @At("TAIL"))
    private void spatialGUI$deferLocalPlayerRenderState(Entity entity, float partialTick, CallbackInfoReturnable<EntityRenderState> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (entity != mc.player) {
            return;
        }

        var renderer = SpatialGUIClient.renderer();
        if (renderer == null || !ConfigManager.config().enabled || ShaderCompat.shaderPackActive() || !(ConfigManager.config().blurWorldBackground || ConfigManager.config().renderPlayerInFront)) {
            WorldBlurRenderer.setDeferredPlayerRenderState(null);
            return;
        }

        if (SpatialGUIRenderer.isExtractingScreen) {
            return;
        }

        boolean defer = renderer.shouldCapture() ? !SpatialGUIClient.getEffectiveFirstPersonMode() : renderer.shouldDeferPlayer();
        WorldBlurRenderer.setDeferredPlayerRenderState(defer ? cir.getReturnValue() : null);
    }

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$skipLocalPlayerSubmit(EntityRenderState renderState, CameraRenderState camera, double x, double y, double z, PoseStack poseStack, SubmitNodeCollector output, CallbackInfo ci) {
        var deferred = WorldBlurRenderer.getDeferredPlayerRenderState();
        if (deferred != null && renderState == deferred) {
            if (!renderState.shadowPieces.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(x, y, z);
                output.submitShadow(poseStack, renderState.shadowRadius, renderState.shadowPieces);
                poseStack.popPose();
            }
            ci.cancel();
        }
    }
    //?}
}