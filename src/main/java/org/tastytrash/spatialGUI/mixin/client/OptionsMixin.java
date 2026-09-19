package org.tastytrash.spatialGUI.mixin.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(Options.class)
public class OptionsMixin {

    @Inject(method = "getCameraType", at = @At("HEAD"), cancellable = true)
    private void diegeticInventory$forceCameraMode(CallbackInfoReturnable<CameraType> cir) {
        var renderer = SpatialGUIClient.renderer();
        if (renderer.shouldCapture() && SpatialGUI.config.enabled) {
            boolean isFirstPerson = (SpatialGUIRenderer.isInventoryScreen() ? SpatialGUI.config.firstPersonModeInventory : SpatialGUI.config.firstPersonModeContainers)
                    || SpatialGUIClient.getSwitchedToFirstPersonDueToBlock();
            if (isFirstPerson) {
                cir.setReturnValue(CameraType.FIRST_PERSON);
            } else {
                cir.setReturnValue(CameraType.THIRD_PERSON_BACK);
            }
        }
    }
}
