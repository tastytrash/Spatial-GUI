package org.tastytrash.spatialGUI.mixin.hud;

import org.spongepowered.asm.mixin.Mixin;

//? if <=1.21.1 {
/*import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;

@Mixin(ToastComponent.class)
public class ToastComponentMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void spatialGUI$drawToastsToMainTarget(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        var renderer = SpatialGUIClient.renderer();
        if (renderer != null && (renderer.shouldCapture() || renderer.isClosing())) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        }
    }
}
*///?} else {
@Mixin(net.minecraft.client.Minecraft.class)
public class ToastComponentMixin {}
//?}