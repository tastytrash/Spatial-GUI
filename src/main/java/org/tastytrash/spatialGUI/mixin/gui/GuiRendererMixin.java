package org.tastytrash.spatialGUI.mixin.gui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
    @ModifyArg(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    //? if > 26.2 {
                    /*target = "Lnet/minecraft/client/gui/render/GuiRenderer;executeDrawRange(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;II)V"
                    *///? } else {
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;executeDrawRange(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;II)V"
                    //? }
            ),
            index = 1
    )
    private RenderTarget diegeticInventory$redirectRenderTarget(RenderTarget original) {
        SpatialGUIRenderer renderer = SpatialGUIClient.renderer();

        if (!renderer.shouldCapture() || !SpatialGUI.config.enabled) {
            return original;
        }

        return renderer.getTarget();
    }
}