package org.tastytrash.spatialGUI.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
//? if >1.21.1 && <26.1.2 {
/*import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.mixin.accessor.GuiGraphicsAccessor;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;
*///?}

@Pseudo
@Mixin(targets = "com.jaquadro.minecraft.storagedrawers.client.gui.StorageGuiGraphics", remap = false)
public abstract class StorageDrawersMixin {
    //? if >1.21.1 && <26.1.2 {
    /*@Inject(method = {
            "<init>(Lnet/minecraft/class_310;Lnet/minecraft/class_11246;II)V",
            "<init>(Lnet/minecraft/class_310;Lnet/minecraft/class_332;II)V"
    }, at = @At("TAIL"), remap = false, require = 0)
    private void spatialGUI$useScreenRenderState(CallbackInfo ci) {
        var renderer = SpatialGUIClient.renderer();
        if (renderer != null && SpatialGUIRenderer.isExtractingScreen) {
            ((GuiGraphicsAccessor) (Object) this).spatialGUI$setGuiRenderState(renderer.getScreenRenderState());
        }
    }
    *///?}
}