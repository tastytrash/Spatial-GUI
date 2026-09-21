package org.tastytrash.spatialGUI.mixin.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void spatialGUI$beginExtract(net.minecraft.client.DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        SpatialGUIRenderer.suppressWindowOverride = true;
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void spatialGUI$endExtract(net.minecraft.client.DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        SpatialGUIRenderer.suppressWindowOverride = false;
    }

    @Redirect(method = "extractRenderState", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"
    ))
    private void spatialGUI$redirectScreenExtraction(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        var renderer = SpatialGUIClient.renderer();
        if (SpatialGUI.config.enabled && screen instanceof AbstractContainerScreen<?> && screen == renderer.getHookedScreen()) {
            SpatialGUIRenderer.suppressWindowOverride = false;
            renderer.extractIsolatedScreen(screen, partialTick);
            SpatialGUIRenderer.suppressWindowOverride = true;
        } else {
            screen.extractRenderStateWithTooltipAndSubtitles(graphics, mouseX, mouseY, partialTick);
        }
    }
}