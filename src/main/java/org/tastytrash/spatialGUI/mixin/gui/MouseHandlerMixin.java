package org.tastytrash.spatialGUI.mixin.gui;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;
import org.tastytrash.spatialGUI.util.MouseHandlerUtil;
import org.tastytrash.spatialGUI.util.RenderUtil;
import org.tastytrash.spatialGUI.util.RenderUtil.QuadBasis;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Unique private static double lastPosX = Double.NaN;
    @Unique private static double lastPosY = Double.NaN;

    @Unique
    private static boolean shouldApplyMouseOverride() {
        Minecraft client = Minecraft.getInstance();
        Screen screen = client.gui.screen();
        return screen instanceof AbstractContainerScreen<?> && org.tastytrash.spatialGUI.SpatialGUI.config.enabled;
    }

    @ModifyReturnValue(method = "getScaledXPos*", at = @At("RETURN"))
    private static double spatialGUI$modifyX(double original) {
        return overrideMousePosition(original, true);
    }

    @ModifyReturnValue(method = "getScaledYPos*", at = @At("RETURN"))
    private static double spatialGUI$modifyY(double original) {
        return overrideMousePosition(original, false);
    }

    @Unique
    private static double overrideMousePosition(double original, boolean isX) {
        if (!shouldApplyMouseOverride()) {
            return original;
        }

        double guiScale = SpatialGUI.config.autoCalculateGuiScale
                ? SpatialGUI.config.calculateAutoGuiScale(Minecraft.getInstance().getWindow().getHeight())
                : SpatialGUI.config.guiScale;

        double srcX, srcY;
        if (SpatialGUIRenderer.isCrosshairModeActive()) {
            Minecraft mc = Minecraft.getInstance();
            srcX = mc.getWindow().getScreenWidth() / 2.0;
            srcY = mc.getWindow().getScreenHeight() / 2.0;
        } else {
            srcX = Minecraft.getInstance().mouseHandler.xpos();
            srcY = Minecraft.getInstance().mouseHandler.ypos();
        }

        var renderer = SpatialGUIClient.renderer();
        if (renderer == null) return original;

        QuadBasis quadBasis = renderer.getInventoryRenderer().getQuadBasis();
        if (quadBasis == null) return original;

        Vector2d mouse = RenderUtil.getInventoryMousePositionRay(srcX, srcY, quadBasis, renderer.getTargetManager().getInventoryTarget());

        if (mouse == null) {
            return isX ? lastPosX : lastPosY;
        }


        lastPosX = mouse.x / guiScale;
        lastPosY = mouse.y / guiScale;

        return isX ? mouse.x / guiScale : mouse.y / guiScale;
    }

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$cancelPlayerRotation(double mousea, CallbackInfo ci) {
        if (SpatialGUIRenderer.isCrosshairModeActive()) {
            ci.cancel();
        }
    }

    //? if >26.2 {
    @Inject(method = "onMove(JDDDD)V", at = @At("HEAD"))
    private void spatialGUI$captureMouseMotion(long handle, double xpos, double ypos, double xrel, double yrel, CallbackInfo ci) {
        if (SpatialGUIRenderer.isCrosshairModeActive()) {
            MouseHandlerUtil.addFreeLookDelta(xrel, yrel);
        }
    }
    //?}
}
