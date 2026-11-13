package org.tastytrash.spatialGUI.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Pseudo
@Mixin(targets = "journeymap.client.event.handlers.ScreenEventHandler", remap = false)
public class JourneyMapMixin {
    @WrapMethod(method = "onScreenPreRender")
    private void spatialGUI$skipMinimapOverride(Screen screen, GuiGraphicsExtractor graphics, int x, int y, Operation<Void> original) {
        if (!SpatialGUIRenderer.isExtractingScreen) {
            original.call(screen, graphics, x, y);
            return;
        }
        boolean prev = SpatialGUIRenderer.skipWindowOverride;
        SpatialGUIRenderer.skipWindowOverride = true;
        try {
            original.call(screen, SpatialGUIRenderer.cursorTarget, x, y);
        } finally {
            SpatialGUIRenderer.skipWindowOverride = prev;
        }
    }
}