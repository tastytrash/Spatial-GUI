package org.tastytrash.spatialGUI.mixin.render;

//? if > 26.2 {
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
//? } else {
//import com.mojang.blaze3d.buffers.GpuBufferSlice;
//import net.minecraft.client.DeltaTracker;
//import org.joml.Matrix4fc;
//? }
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
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
    //? if >26.2 {
    private void diegeticInventory$renderScreen(GraphicsResourceAllocator resourceAllocator, boolean renderOutline, CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, boolean consistentDepthRequired, CallbackInfo ci) {
        if (!SpatialGUI.config.enabled) {
            return;
        }
        var camera = Minecraft.getInstance().gameRenderer.mainCamera();
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(new Quaternionf()
                .rotateX((float) Math.toRadians(camera.xRot()))
                .rotateY((float) Math.toRadians(camera.yRot() + 180.0f))
                .get(new Matrix4f())
        );
        SpatialGUIClient.renderer().renderInWorld(poseStack);
    }
    //? } else {
//    private void diegeticInventory$renderScreen(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
//        if (!SpatialGUI.config.enabled) {
//            return;
//        }
//        PoseStack poseStack = new PoseStack();
//        poseStack.mulPose(modelViewMatrix);
//        SpatialGUIClient.renderer().renderInWorld(poseStack);
//    }
    //? }
}