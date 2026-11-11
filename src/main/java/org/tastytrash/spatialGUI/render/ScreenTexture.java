package org.tastytrash.spatialGUI.render;

import org.tastytrash.spatialGUI.config.ConfigManager;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
//? if <1.21.11 {
//?} else if >26.2 {
/*import com.mojang.renderpearl.api.GpuFormat;
import org.joml.Vector4f;
*///?} else if 26.2 {
/*import org.joml.Vector4f;
import com.mojang.blaze3d.GpuFormat;
*///?}

public class ScreenTexture {
    private TextureTarget target;

    public void clearTarget() {
        if (target == null) {
            return;
        }

        //? if <1.21.11 {
        /*target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        target.clear(Minecraft.ON_OSX);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        *///?} else {
        var colorTexture = target.getColorTexture();
        var depthTexture = target.getDepthTexture();

        if (colorTexture == null) {
            return;
        }

        var encoder = RenderSystem.getDevice().createCommandEncoder();
        //? if >=26.2 {
        /*encoder.clearColorTexture(colorTexture, new Vector4f(0.0F, 0.0F, 0.0F, 0.0F));
        if (depthTexture != null) {
            encoder.clearDepthTexture(depthTexture, 1.0);
        }
        *///?} else {
        if (depthTexture != null) {
            encoder.clearColorAndDepthTextures(colorTexture, 0, depthTexture, 1.0);
        } else {
            encoder.clearColorTexture(colorTexture, 0);
        }
        //?}
        //?}
    }

    public void prepareTarget() {
        Minecraft client = Minecraft.getInstance();
        int windowWidth = client.getWindow().getWidth();
        int windowHeight = client.getWindow().getHeight();

        if (windowWidth <= 0 || windowHeight <= 0) {
            return;
        }

        double scale = Mth.clamp(ConfigManager.config().renderScalePercent, 25, 100) / 100.0;
        int width = Math.max(1, (int) Math.round(windowWidth * scale));
        int height = Math.max(1, (int) Math.round(windowHeight * scale));

        if (target == null) {
            //? if <1.21.11 {
            /*target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
            *///?} else {
            target = new TextureTarget(
                    "Spatial GUI Screen",
                    width,
                    height,
                    //? if >26.2 {
                    /*GpuFormat.RGBA8_UNORM,
                    GpuFormat.D16_UNORM
                    *///?} else if 26.2 {
                    /*true,
                    GpuFormat.RGBA8_UNORM
                    *///?} else {
                    true
                    //?}
            );
            //?}
            clearTarget();
            return;
        }

        if (target.width != width || target.height != height) {
            //? if <1.21.11 {
            /*target.resize(width, height, Minecraft.ON_OSX);
            *///?} else {
            target.resize(width, height);
            //?}
            clearTarget();
        }
    }

    public TextureTarget prepareAndGetTarget() {
        prepareTarget();
        return target;
    }

    public TextureTarget getTarget() {
        return target;
    }
}