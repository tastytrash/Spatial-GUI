package org.tastytrash.spatialGUI.mixin.gui;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Unique private static double lastPosX = Double.NaN;
    @Unique private static double lastPosY = Double.NaN;

    @Unique
    private static boolean shouldApplyMouseOverride() {
        Minecraft client = Minecraft.getInstance();
        Screen screen = client.gui.screen();
        return screen instanceof AbstractContainerScreen<?> && org.tastytrash.spatialGUI.SpatialGUI.config.enabled;
    }

    @ModifyReturnValue(method = "getScaledXPos*", at = @At("RETURN"))
    private static double spatialGUI$modifyX(double original) {
        return overrideMousePosition(original, true);
    }

    @ModifyReturnValue(method = "getScaledYPos*", at = @At("RETURN"))
    private static double spatialGUI$modifyY(double original) {
        return overrideMousePosition(original, false);
    }

    @Unique
    private static double overrideMousePosition(double original, boolean isX) {
        if (!shouldApplyMouseOverride()) {
            return original;
        }

        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        Vector2d mouse = SpatialGUIRenderer.getInventoryMousePosition(
                Minecraft.getInstance().mouseHandler.xpos(),
                Minecraft.getInstance().mouseHandler.ypos()
        );

        if (mouse == null) {
            return isX ? lastPosX : lastPosY;
        }

        lastPosX = mouse.x / guiScale;
        lastPosY = mouse.y / guiScale;

        return isX ? mouse.x / guiScale + 0.01 : mouse.y / guiScale;
    }
}
