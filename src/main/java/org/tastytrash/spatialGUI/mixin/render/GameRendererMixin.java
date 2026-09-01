package org.tastytrash.spatialGUI.mixin.render;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Final @Shadow private GameRenderState gameRenderState;
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render()V"))
    private void spatialGUI$beforeGuiRender(CallbackInfo ci) {
        var renderer = SpatialGUIClient.renderer();

        if (renderer.shouldCapture() && SpatialGUI.config.enabled) {
            renderer.prepareTarget();
            renderer.clearTarget();
        }
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$overrideHideHand(CallbackInfo ci) {
        var renderer = SpatialGUIClient.renderer();
        if (renderer.shouldCapture() && SpatialGUI.config.enabled && !SpatialGUIRenderer.hadHideHUD) {
            this.gameRenderState.guiRenderState.isHudHidden = false;
            
            if (SpatialGUIClient.getEffectiveFirstPersonMode() && SpatialGUI.config.hideHandsInFirstPerson) {
                ci.cancel();
            }
        }
    }
}