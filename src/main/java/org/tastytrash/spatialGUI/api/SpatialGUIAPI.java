package org.tastytrash.spatialGUI.api;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.phys.Vec3;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

public final class SpatialGUIAPI {
    private SpatialGUIAPI() {}

    /** Checks if Spatial GUI is enabled and showing a screen in 3D.
     * @return {@code true} if Spatial GUI is enabled and showing a screen in 3D */
    public static boolean isActive() {
        return SpatialGUIClient.isEnabled()
                && renderer() != null
                && renderer().getHookedScreen() != null;
    }

    /** Gets the screen currently shown in 3D.
     * @return the screen currently shown in 3D, or null if none */
    public static Screen getCurrentScreen() {
        return renderer() != null ? renderer().getHookedScreen() : null;
    }

    /** Checks if Spatial GUI is in first person mode.
     * @return {@code true} if in first person mode */
    public static boolean isFirstPersonMode() {
        return isActive() && SpatialGUIClient.getEffectiveFirstPersonMode();
    }

    /** Checks if Spatial GUI is in third person mode.
     * @return {@code true} if in third person mode */
    public static boolean isThirdPersonMode() {
        return isActive() && !SpatialGUIClient.getEffectiveFirstPersonMode();
    }

    /** Checks if the HUD is being hidden.
     * @return {@code true} if the HUD is being hidden */
    public static boolean shouldHideHud() {
        return SpatialGUIClient.shouldHideHud();
    }

    /** Checks if the hotbar is being hidden.
     * @return {@code true} if the hotbar is being hidden */
    public static boolean shouldHideHotbar() {
        return SpatialGUIClient.shouldHideHotbar();
    }

    /** Checks if the mouse is over the 3D screen.
     * @return {@code true} if the mouse is over the 3D screen */
    public static boolean isMouseOverScreen() {
        return renderer() != null && renderer().isMouseOverScreen();
    }

    /** Checks if a screen is currently opening.
     * @return {@code true} if a screen is playing its opening animation */
    public static boolean isOpeningScreen() {
        return renderer() != null && renderer().getWorldScreenRenderer().isOpening();
    }

    /** Checks if a screen is currently closing.
     * @return {@code true} if a screen is playing its closing animation */
    public static boolean isClosingScreen() {
        return renderer() != null && renderer().isClosing();
    }

    /** Gets the camera position Spatial GUI is using.
     * @return the camera position Spatial GUI is using */
    public static Vec3 getCameraPosition() {
        return SpatialGUIClient.getCameraPosition();
    }

    /** Checks if crosshair mode is on, where the crosshair acts as the mouse in first person.
     * @return {@code true} if crosshair mode is on */
    public static boolean isCrosshairModeActive() {
        return SpatialGUIRenderer.isCrosshairModeActive();
    }

    private static SpatialGUIRenderer renderer() {
        return SpatialGUIClient.renderer();
    }
}