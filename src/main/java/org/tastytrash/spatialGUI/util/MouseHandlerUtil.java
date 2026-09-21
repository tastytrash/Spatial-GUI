package org.tastytrash.spatialGUI.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.mixin.gui.MouseHandlerAccessor;

public class MouseHandlerUtil {
    private static boolean weGrabbedMouse = false;

    public static void grabMouseForFirstPerson() {
        Minecraft mc = Minecraft.getInstance();
        MouseHandlerAccessor accessor = (MouseHandlerAccessor) mc.mouseHandler;
        if (!accessor.getMouseGrabbed()) {
            accessor.setMouseGrabbed(true);
            double centerX = mc.getWindow().getScreenWidth() / 2.0;
            double centerY = mc.getWindow().getScreenHeight() / 2.0;
            //? if >26.2 {
            InputConstants.grabMouse(mc.getWindow(), centerX, centerY);
            //?} else {
            // InputConstants.grabOrReleaseMouse(mc.getWindow(), InputConstants.CURSOR_DISABLED, centerX, centerY);
            //?}
            mc.mouseHandler.setIgnoreFirstMove();
        }
        weGrabbedMouse = true;
    }

    public static void releaseMouseFromFirstPerson() {
        if (!weGrabbedMouse) return;
        Minecraft mc = Minecraft.getInstance();
        MouseHandlerAccessor accessor = (MouseHandlerAccessor) mc.mouseHandler;
        if (accessor.getMouseGrabbed()) {
            accessor.setMouseGrabbed(false);
            double centerX = mc.getWindow().getScreenWidth() / 2.0;
            double centerY = mc.getWindow().getScreenHeight() / 2.0;
            //? if >26.2 {
            InputConstants.releaseMouse(mc.getWindow(), centerX, centerY);
            //?} else {
            // InputConstants.grabOrReleaseMouse(mc.getWindow(), InputConstants.CURSOR_NORMAL, centerX, centerY);
            //?}
        }
        weGrabbedMouse = false;
    }

    public static void updateMouseGrabForFirstPerson(boolean isFirstPerson) {
        if (!SpatialGUI.config.useCrosshairForFirstPerson || !isFirstPerson) {
            releaseMouseFromFirstPerson();
        } else {
            grabMouseForFirstPerson();
        }
    }
}
