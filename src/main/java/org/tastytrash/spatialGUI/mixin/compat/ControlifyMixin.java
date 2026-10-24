//? if controlify {
package org.tastytrash.spatialGUI.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.compat.ControlifyCompat;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;
import org.tastytrash.spatialGUI.util.MouseHandlerUtil;

//? if <=1.21.1 {
/*import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
*///?}

@Pseudo
@Mixin(targets = "dev.isxander.controlify.virtualmouse.VirtualMouseHandler", remap = false)
public class ControlifyMixin {
    @Unique
    private static boolean spatialGUI$active() {
        if (!SpatialGUIClient.isEnabled()) return false;
        var renderer = SpatialGUIClient.renderer();
        return renderer != null && SpatialGUIClient.shouldHookScreen(renderer.getHookedScreen());
    }

    @ModifyVariable(method = "renderVirtualMouse", at = @At("STORE"), ordinal = 0, require = 0, remap = false)
    private double spatialGUI$cursorX(double original) {
        return spatialGUI$active() ? MouseHandlerUtil.getLastPos(true, original) : original;
    }

    @ModifyVariable(method = "renderVirtualMouse", at = @At("STORE"), ordinal = 1, require = 0, remap = false)
    private double spatialGUI$cursorY(double original) {
        return spatialGUI$active() ? MouseHandlerUtil.getLastPos(false, original) : original;
    }

    @Inject(method = "renderVirtualMouse", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void spatialGUI$hideInCrosshair(CallbackInfo ci) {
        if (SpatialGUIRenderer.isCrosshairModeActive()) ci.cancel();
    }

    //? if neoforge {
    /*@Inject(method = "renderVirtualMouse", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void spatialGUI$onlyDrawInIsolated(CallbackInfo ci) {
        if (spatialGUI$active() && !SpatialGUIRenderer.isExtractingScreen) ci.cancel();
    }
    *///?}

    @Inject(method = {"updateMouse", "snapToPoint"}, at = @At("HEAD"), require = 0, remap = false)
    private void spatialGUI$moveStart(CallbackInfo ci) {
        ControlifyCompat.inVirtualMove = true;
    }

    @Inject(method = {"updateMouse", "snapToPoint"}, at = @At("RETURN"), require = 0, remap = false)
    private void spatialGUI$moveEnd(CallbackInfo ci) {
        ControlifyCompat.inVirtualMove = false;
    }

    @Inject(method = "handleScroll", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void spatialGUI$stopScrollInCrosshair(CallbackInfo ci) {
        if (SpatialGUIRenderer.isCrosshairModeActive()) ci.cancel();
    }

    //? if <=1.21.1 {
    /*@Inject(method = "renderVirtualMouse", at = @At("HEAD"), require = 0, remap = false)
    private void spatialGUI$keepAlphaStart(CallbackInfo ci) {
        if (SpatialGUIRenderer.isExtractingScreen) {
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }
    }

    @Inject(method = "renderVirtualMouse", at = @At("RETURN"), require = 0, remap = false)
    private void spatialGUI$keepAlphaEnd(CallbackInfo ci) {
        if (SpatialGUIRenderer.isExtractingScreen) RenderSystem.defaultBlendFunc();
    }
    *///?}
}
//?}