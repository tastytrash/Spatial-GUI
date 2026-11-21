package org.tastytrash.spatialGUI.mixin.compat;

//? if forge {
/*import net.minecraftforge.client.event.ViewportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

@Pseudo
@Mixin(targets = "io.socol.betterthirdperson.CustomCameraListener", remap = false)
public class BetterThirdPersonMixin {
    @Inject(method = "onCameraSetup", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spatialGUI$skipWhileActive(ViewportEvent.ComputeCameraAngles event, CallbackInfo ci) {
        var renderer = SpatialGUIClient.renderer();
        if (renderer != null && SpatialGUIClient.isEnabled() && (renderer.shouldCapture() || renderer.isClosingWithCameraTransition())) {
            ci.cancel();
        }
    }
}
*///?}
