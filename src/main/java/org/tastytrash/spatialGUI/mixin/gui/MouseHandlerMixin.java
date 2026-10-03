package org.tastytrash.spatialGUI.mixin.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.joml.Vector2d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.compat.InventoryParticlesCompat;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;
import org.tastytrash.spatialGUI.util.MouseHandlerUtil;
import org.tastytrash.spatialGUI.util.RenderUtil;
import org.tastytrash.spatialGUI.util.RenderUtil.QuadBasis;

@Mixin(value = MouseHandler.class, priority = 1100)
public class MouseHandlerMixin {

    //? if <=1.21.1 {
    /*@Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;
    *///?}

    @Unique
    private static boolean shouldApplyMouseOverride() {
        if (!SpatialGUIClient.isEnabled()) return false;
        Minecraft client = Minecraft.getInstance();
        //? if >=26.2 {
        /*Screen screen = client.screen;
         *///?} else {
        Screen screen = client.screen;
        //?}
        if (SpatialGUIClient.shouldHookScreen(screen)) return true;
        var renderer = SpatialGUIClient.renderer();
        return renderer != null && SpatialGUIClient.shouldHookScreen(renderer.getHookedScreen());
    }

    //? if >1.21.1 {
    @ModifyReturnValue(method = "getScaledXPos(Lcom/mojang/blaze3d/platform/Window;)D", at = @At("RETURN"))
    private static double spatialGUI$modifyX(double original) {
        return overrideMousePosition(original, true);
    }

    @ModifyReturnValue(method = "getScaledYPos(Lcom/mojang/blaze3d/platform/Window;)D", at = @At("RETURN"))
    private static double spatialGUI$modifyY(double original) {
        return overrideMousePosition(original, false);
    }
    //?} else {
    /*@ModifyExpressionValue(
            //? if >1.20.1 {
            method = {"onPress", "onScroll", "handleAccumulatedMovement"},
            //?} else {
            /^method = {"onPress", "onScroll"},
            ^///?}
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;xpos:D", opcode = Opcodes.GETFIELD)
    )
    private double spatialGUI$modifyRawX(double original) {
        return overrideRawPosition(original, true);
    }

    @ModifyExpressionValue(
            //? if >1.20.1 {
            method = {"onPress", "onScroll", "handleAccumulatedMovement"},
            //?} else {
            /^method = {"onPress", "onScroll"},
            ^///?}
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;ypos:D", opcode = Opcodes.GETFIELD)
    )
    private double spatialGUI$modifyRawY(double original) {
        return overrideRawPosition(original, false);
    }
    *///?}

    @ModifyReturnValue(method = "xpos", at = @At("RETURN"))
    private double spatialGUI$modifyRawXpos(double original) {
        return overrideRawPosition(original, true);
    }

    @ModifyReturnValue(method = "ypos", at = @At("RETURN"))
    private double spatialGUI$modifyRawYpos(double original) {
        return overrideRawPosition(original, false);
    }

    @Unique
    private static double overrideRawPosition(double raw, boolean isX) {
        if (!shouldApplyMouseOverride()) {
            return raw;
        }

        Window window = Minecraft.getInstance().getWindow();
        double toScaled = isX
                ? (double) window.getGuiScaledWidth() / (double) window.getScreenWidth()
                : (double) window.getGuiScaledHeight() / (double) window.getScreenHeight();

        if (toScaled == 0.0) {
            return raw;
        }

        double scaled = overrideMousePosition(raw * toScaled, isX);
        if (Double.isNaN(scaled)) {
            return raw;
        }
        return scaled / toScaled;
    }

    @Unique
    private static double overrideMousePosition(double original, boolean isX) {
        if (!shouldApplyMouseOverride()) {
            return original;
        }

        Minecraft mc = Minecraft.getInstance();
        double guiScale = SpatialGUI.config.getEffectiveGuiScale(mc.getWindow().getWidth(), mc.getWindow().getHeight());

        double srcX, srcY;
        if (SpatialGUIRenderer.isCrosshairModeActive()) {
            srcX = mc.getWindow().getScreenWidth() / 2.0;
            srcY = mc.getWindow().getScreenHeight() / 2.0;
        } else {
            srcX = ((MouseHandlerAccessor) mc.mouseHandler).getRawXpos();
            srcY = ((MouseHandlerAccessor) mc.mouseHandler).getRawYpos();
        }

        var renderer = SpatialGUIClient.renderer();
        if (renderer == null) return MouseHandlerUtil.getLastPos(isX, original);

        QuadBasis quadBasis = renderer.getInventoryRenderer().getQuadBasis();
        if (quadBasis == null) return MouseHandlerUtil.getLastPos(isX, original);

        RenderUtil.CylinderBasis cylinderBasis = renderer.getInventoryRenderer().getCylinderBasis();

        Vector2d mouse = MouseHandlerUtil.getOrComputeMousePosition(
                srcX, srcY, quadBasis, cylinderBasis, guiScale, renderer.getTargetManager().getInventoryTarget()
        );

        return MouseHandlerUtil.getLastPos(isX, original);
    }

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    //? if >1.20.1 {
    private void spatialGUI$cancelPlayerRotation(double mousea, CallbackInfo ci) {
    //?} else {
    /*private void spatialGUI$cancelPlayerRotation(CallbackInfo ci) {
    *///?}
        if (SpatialGUIRenderer.isCrosshairModeActive()) {
            //? if <=1.21.1 {
            /*MouseHandlerUtil.addFreeLookDelta(this.accumulatedDX, this.accumulatedDY);
            this.accumulatedDX = 0.0;
            this.accumulatedDY = 0.0;
            *///?}
            ci.cancel();
        }
    }

    //? if >26.2 {
    /*@Inject(method = "onMove(JDDDD)V", at = @At("HEAD"))
    private void spatialGUI$captureMouseMotion(long handle, double xpos, double ypos, double xrel, double yrel, CallbackInfo ci) {
        if (SpatialGUIRenderer.isCrosshairModeActive()) {
            MouseHandlerUtil.addFreeLookDelta(xrel, yrel);
        }
    }

    @Inject(method = "onMove(JDDDD)V", at = @At("TAIL"))
    private void spatialGUI$syncInventoryParticles(long handle, double xpos, double ypos, double xrel, double yrel, CallbackInfo ci) {
        if (shouldApplyMouseOverride()) {
            InventoryParticlesCompat.updateCursor(MouseHandlerUtil.getLastPos(true), MouseHandlerUtil.getLastPos(false));
        }
    }
    *///?} else {
    @Inject(method = "onMove(JDD)V", at = @At("TAIL"))
    private void spatialGUI$syncInventoryParticles(long handle, double xpos, double ypos, CallbackInfo ci) {
        if (shouldApplyMouseOverride()) {
            InventoryParticlesCompat.updateCursor(MouseHandlerUtil.getLastPos(true), MouseHandlerUtil.getLastPos(false));
        }
    }
    //?}

    @Unique
    private double spatialGUI$preReleaseX;

    @Unique
    private double spatialGUI$preReleaseY;

    @Inject(method = "releaseMouse", at = @At("HEAD"))
    private void spatialGUI$capturePreRelease(CallbackInfo ci) {
        var spatialGUI$acc = (MouseHandlerAccessor) (Object) this;
        spatialGUI$preReleaseX = spatialGUI$acc.getRawXpos();
        spatialGUI$preReleaseY = spatialGUI$acc.getRawYpos();
    }

    @Unique
    private static boolean spatialGUI$crosshairIncoming() {
        return SpatialGUI.config.useCrosshairForFirstPerson && SpatialGUIClient.getEffectiveFirstPersonMode();
    }

    @Unique
    private static Screen spatialGUI$currentScreen(Minecraft spatialGUI$mc) {
        //? if >=26.2 {
        /*return spatialGUI$mc.gui.screen();
         *///?} else {
        return spatialGUI$mc.screen;
        //?}
    }

    @Unique
    private double spatialGUI$warpArgX(double original) {
        Minecraft spatialGUI$mc = Minecraft.getInstance();
        Screen spatialGUI$screen = spatialGUI$currentScreen(spatialGUI$mc);
        if (spatialGUI$crosshairIncoming() || spatialGUI$screen == null || !SpatialGUIClient.shouldHookScreen(spatialGUI$screen)) return original;
        return spatialGUI$preReleaseX;
    }

    @Unique
    private double spatialGUI$warpArgY(double original) {
        Minecraft spatialGUI$mc = Minecraft.getInstance();
        Screen spatialGUI$screen = spatialGUI$currentScreen(spatialGUI$mc);
        if (spatialGUI$crosshairIncoming() || spatialGUI$screen == null || !SpatialGUIClient.shouldHookScreen(spatialGUI$screen)) return original;
        return spatialGUI$preReleaseY;
    }

    //? if <=1.21.1 {
    @ModifyArg(method = "releaseMouse", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"
    ), index = 2)
    private double spatialGUI$warpX1(double original) {
        return spatialGUI$warpArgX(original);
    }

    @ModifyArg(method = "releaseMouse", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"
    ), index = 3)
    private double spatialGUI$warpY1(double original) {
        return spatialGUI$warpArgY(original);
    }
    //?} else if <=1.21.11 {
    /*@ModifyArg(method = "releaseMouse", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(Lcom/mojang/blaze3d/platform/Window;IDD)V"
    ), index = 2)
    private double spatialGUI$warpX2(double original) {
        return spatialGUI$warpArgX(original);
    }

    @ModifyArg(method = "releaseMouse", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(Lcom/mojang/blaze3d/platform/Window;IDD)V"
    ), index = 3)
    private double spatialGUI$warpY2(double original) {
        return spatialGUI$warpArgY(original);
    }
    *///?}

    @Inject(method = "releaseMouse", at = @At("TAIL"))
    private void spatialGUI$restoreCursorFields(CallbackInfo ci) {
        Minecraft spatialGUI$mc = Minecraft.getInstance();
        Screen spatialGUI$screen = spatialGUI$currentScreen(spatialGUI$mc);
        if (spatialGUI$crosshairIncoming() || spatialGUI$screen == null || !SpatialGUIClient.shouldHookScreen(spatialGUI$screen)) return;
        var spatialGUI$acc = (MouseHandlerAccessor) (Object) this;
        spatialGUI$acc.setRawXpos(spatialGUI$preReleaseX);
        spatialGUI$acc.setRawYpos(spatialGUI$preReleaseY);
    }

}
