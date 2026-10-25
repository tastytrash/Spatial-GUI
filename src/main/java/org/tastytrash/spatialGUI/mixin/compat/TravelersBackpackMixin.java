package org.tastytrash.spatialGUI.mixin.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.util.GuiScaleUtil;

@Pseudo
@Mixin(targets = "com.tiviacz.travelersbackpack.client.screens.AbstractBackpackScreen", remap = false)
public abstract class TravelersBackpackMixin {
    @Inject(method = "updateDimensions", at = @At("HEAD"), remap = false, require = 0)
    private void spatialGUI$pendingScreenStart(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (GuiScaleUtil.scaledHeight(Minecraft.getInstance().getWindow(), self) >= 132) {
            GuiScaleUtil.pendingScreen = self;
        }
    }

    @Inject(method = "updateDimensions", at = @At("RETURN"), remap = false, require = 0)
    private void spatialGUI$forceScreenScaleEnd(CallbackInfo ci) {
        GuiScaleUtil.pendingScreen = null;
    }
}