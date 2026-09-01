package org.tastytrash.spatialGUI.mixin.gui;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.tastytrash.spatialGUI.SpatialGUI;

@Mixin(Window.class)
public class WindowMixin {

    @ModifyReturnValue(method = "getGuiScale", at = @At("RETURN"))
    private int spatialGUI$overrideGuiScale(int original) {
        return shouldOverride() ? getGuiScale() : original;
    }

    @ModifyReturnValue(method = "getGuiScaledWidth", at = @At("RETURN"))
    private int spatialGUI$overrideScaledWidth(int original) {
        return shouldOverride() ? calculateScaledDimension(true) : original;
    }

    @ModifyReturnValue(method = "getGuiScaledHeight", at = @At("RETURN"))
    private int spatialGUI$overrideScaledHeight(int original) {
        return shouldOverride() ? calculateScaledDimension(false) : original;
    }

    @Unique
    private int calculateScaledDimension(boolean isWidth) {
        Window self = (Window)(Object) this;
        double dimension = isWidth ? self.getWidth() : self.getHeight();
        return Mth.ceil(dimension / (double) getGuiScale());
    }

    @Unique
    private int getGuiScale() {
        if (SpatialGUI.config.autoCalculateGuiScale) {
            Window self = (Window)(Object) this;
            return SpatialGUI.config.calculateAutoGuiScale(self.getHeight());
        }
        return SpatialGUI.config.guiScale;
    }

    @Unique
    private static boolean shouldOverride() {
        Screen screen = Minecraft.getInstance().gui.screen();
        return screen instanceof AbstractContainerScreen<?>
                && SpatialGUI.config.enabled;
    }
}