package org.tastytrash.spatialGUI.mixin.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SubtitleOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

@Mixin(SubtitleOverlay.class)
public class SubtitleOverlayMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$hideSubtitles(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (SpatialGUIClient.renderer().shouldCapture() && SpatialGUI.config.enabled) {
            ci.cancel();
        }
    }
}
