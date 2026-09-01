package org.tastytrash.spatialGUI.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

public class SpatialGUIClient implements ClientModInitializer {

    private static SpatialGUIRenderer renderer;
    private static boolean effectiveFirstPersonMode = false;
    private static boolean switchedToFirstPersonDueToBlock = false;

    @Override
    public void onInitializeClient() {
        renderer = new SpatialGUIRenderer();

        ScreenEvents.BEFORE_INIT.register((_, screen, _, _) -> {
            if (screen instanceof AbstractContainerScreen<?> && org.tastytrash.spatialGUI.SpatialGUI.config.enabled) {
                renderer.hookScreen(screen);
            }
        });
    }

    public static SpatialGUIRenderer renderer() {
        return renderer;
    }

    public static boolean getEffectiveFirstPersonMode() {
        return effectiveFirstPersonMode;
    }

    public static void setEffectiveFirstPersonMode(boolean value) {
        effectiveFirstPersonMode = value;
    }

    public static boolean getSwitchedToFirstPersonDueToBlock() {
        return switchedToFirstPersonDueToBlock;
    }

    public static void setSwitchedToFirstPersonDueToBlock(boolean value) {
        switchedToFirstPersonDueToBlock = value;
    }
}