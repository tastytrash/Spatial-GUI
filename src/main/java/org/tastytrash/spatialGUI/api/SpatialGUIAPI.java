package org.tastytrash.spatialGUI.api;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.phys.Vec3;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

public final class SpatialGUIAPI {
    private SpatialGUIAPI() {}

    // Returns true if Spatial GUI is enabled and showing a screen in 3D
    public static boolean isActive() {
        return SpatialGUIClient.isEnabled()
                && renderer() != null
                && renderer().getHookedScreen() != null;
    }

    // Gets the screen currently shown in 3D, or null if none
    public static Screen getCurrentScreen() {
        return renderer() != null ? renderer().getHookedScreen() : null;
    }

    // Returns true if in first person mode
    public static boolean isFirstPersonMode() {
        return isActive() && SpatialGUIClient.getEffectiveFirstPersonMode();
    }

    // Returns true if in third person mode
    public static boolean isThirdPersonMode() {
        return isActive() && !SpatialGUIClient.getEffectiveFirstPersonMode();
    }

    // Returns true if the HUD is being hidden
    public static boolean shouldHideHud() {
        return SpatialGUIClient.shouldHideHud();
    }

    // Returns true if the hotbar is being hidden
    public static boolean shouldHideHotbar() {
        return SpatialGUIClient.shouldHideHotbar();
    }

    // Returns true if the mouse is over the 3D screen
    public static boolean isMouseOverScreen() {
        return renderer() != null && renderer().isMouseOverScreen();
    }

    // Returns true if a screen is currently opening (playing opening animation)
    public static boolean isOpeningScreen() {
        return renderer() != null && renderer().getInventoryRenderer().isOpening();
    }

    // Returns true if a screen is currently closing (playing closing animation)
    public static boolean isClosingScreen() {
        return renderer() != null && renderer().isClosing();
    }

    // Gets the camera position Spatial GUI is using
    public static Vec3 getCameraPosition() {
        return SpatialGUIClient.getCameraPosition();
    }

    // Returns true if crosshair mode is on (using crosshair for mouse in first person)
    public static boolean isCrosshairModeActive() {
        return SpatialGUIRenderer.isCrosshairModeActive();
    }

    private static SpatialGUIRenderer renderer() {
        return SpatialGUIClient.renderer();
    }
}
