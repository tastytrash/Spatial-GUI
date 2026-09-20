package org.tastytrash.spatialGUI.compat;

import net.minecraft.client.Minecraft;

public class BetterF1Compat {
    private static Object savedState = null;

    public static boolean isLoaded() {
        try {
            Class.forName("com.movtery.betterf1.BetterF1");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static Object getState() {
        try {
            Class<?> betterF1Class = Class.forName("com.movtery.betterf1.BetterF1");
            return betterF1Class.getField("state").get(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object getEnum(String name) {
        try {
            Class<?> stateEnumClass = Class.forName("com.movtery.betterf1.client.HUDState");
            return stateEnumClass.getField(name).get(null);
        } catch (Exception e) {
            return null;
        }
    }

    public static void saveState() {
        savedState = getState();
    }

    public static void hide() {
        Object allHidden = getEnum("ALL_HIDDEN");
        if (allHidden == null) return;

        for (int i = 0; i < 3; i++) {
            if (allHidden.equals(getState())) break;
            Minecraft.getInstance().gui.hud.toggle();
        }
    }

    public static void restoreState() {
        if (savedState == null) return;

        for (int i = 0; i < 3; i++) {
            if (savedState.equals(getState())) break;
            Minecraft.getInstance().gui.hud.toggle();
        }
        savedState = null;
    }
}
