package org.tastytrash.spatialGUI.render;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.joml.Vector4f;
//? > 26.2 {
import com.mojang.renderpearl.api.GpuFormat;
//? } else {
/*import com.mojang.blaze3d.GpuFormat;
*///? }

public class TextureTargetManager {
    private TextureTarget inventoryTarget;

    public void clearTarget() {
        if (inventoryTarget == null) {
            return;
        }

        var colorTexture = inventoryTarget.getColorTexture();
        var depthTexture = inventoryTarget.getDepthTexture();

        if (colorTexture == null) {
            return;
        }

        var encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.clearColorTexture(colorTexture, new Vector4f(0.0F, 0.0F, 0.0F, 0.0F));

        if (depthTexture != null) {
            encoder.clearDepthTexture(depthTexture, 1.0);
        }
    }

    public void prepareTarget() {
        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getWidth();
        int height = client.getWindow().getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        if (inventoryTarget == null) {
            inventoryTarget = new TextureTarget(
                    "Spatial GUI Inventory",
                    width,
                    height,
                    //? if >26.2 {
                    GpuFormat.RGBA8_UNORM,
                    GpuFormat.D16_UNORM
                    //?} else {
                    /*true,
                    GpuFormat.RGBA8_UNORM
                    *///?}
            );
            return;
        }

        if (inventoryTarget.width != width || inventoryTarget.height != height) {
            inventoryTarget.resize(width, height);
        }
    }

    public TextureTarget getTarget() {
        prepareTarget();
        return inventoryTarget;
    }

    public TextureTarget getInventoryTarget() {
        return inventoryTarget;
    }
}
