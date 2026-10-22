package org.tastytrash.spatialGUI.mixin.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.editor.EditorMode;
import org.tastytrash.spatialGUI.editor.EditorScreen;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Shadow private CameraType cameraType;

    @Inject(method = "getCameraType", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$forceCameraMode(CallbackInfoReturnable<CameraType> cir) {
        var renderer = SpatialGUIClient.renderer();
        if (renderer.shouldCapture() && SpatialGUIClient.isEnabled()) {
            SpatialGUIClient.setWasThirdPersonCamera(this.cameraType != CameraType.FIRST_PERSON);
            SpatialGUIClient.setOriginalCameraType(this.cameraType);
            Screen hooked = renderer.getHookedScreen();
            boolean editorOverride = hooked instanceof EditorScreen && EditorMode.viewOverride() != null;
            if (ConfigManager.config().autoDetectCameraMode && !editorOverride) {
                return;
            }
            boolean isFirstPerson = SpatialGUIClient.shouldUseFirstPersonMode(hooked);
            if (isFirstPerson) {
                cir.setReturnValue(CameraType.FIRST_PERSON);
            } else {
                cir.setReturnValue(CameraType.THIRD_PERSON_BACK);
            }
        } else if (renderer.isClosingThirdToFirstPerson() && SpatialGUIClient.isEnabled()) {
            cir.setReturnValue(CameraType.THIRD_PERSON_BACK);
        }
    }
}
