package org.tastytrash.spatialGUI.mixin.input;

import org.spongepowered.asm.mixin.Mixin;

//? if >=1.21.11 {
import com.mojang.blaze3d.platform.cursor.CursorType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsCursorMixin {
    @Inject(method = "requestCursor", at = @At("HEAD"))
    private void spatialGUI$relayCursor(CursorType cursorType, CallbackInfo ci) {
        GuiGraphicsExtractor target = SpatialGUIRenderer.cursorTarget;
        if (target != null && target != (Object) this) {
            target.requestCursor(cursorType);
        }
    }
}
//?} else {
/*@Mixin(net.minecraft.client.gui.Gui.class)
public class GuiGraphicsCursorMixin {}
*///?}