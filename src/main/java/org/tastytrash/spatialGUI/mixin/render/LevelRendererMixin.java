package org.tastytrash.spatialGUI.mixin.render;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void diegeticInventory$renderScreen(GraphicsResourceAllocator resourceAllocator, boolean renderOutline, CameraRenderState cameraState, com.mojang.renderpearl.api.buffers.GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, boolean consistentDepthRequired, CallbackInfo ci) {
        if (!SpatialGUI.config.enabled) {
            return;
        }
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(RenderSystem.getModelViewMatrixCopy());
        SpatialGUIClient.renderer().renderInWorld(poseStack);
    }
}