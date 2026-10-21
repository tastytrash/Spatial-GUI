package org.tastytrash.spatialGUI.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

public class EditorMode {
    private static Boolean viewOverride;
    private static boolean previewing;

    public static final String[] HINTS = {
        "Scroll: Screen distance",
        "RMB: Reset handle",
        "V: Toggle first/third person",
        "Ctrl+Z: Undo",
        "Ctrl+Y: Redo",
        "Space: Preview"
    };

    public static void toggle(Minecraft client) {
        if (client.level == null) return;
        Screen screen = client.screen;
        SpatialGUIClient.showScreen(client, screen instanceof EditorScreen ? null : new EditorScreen());
    }

    public static void begin() {
        viewOverride = null;
        previewing = false;
    }

    public static void end() {
        viewOverride = null;
    }

    public static Boolean viewOverride() {
        return viewOverride;
    }

    public static void toggleView() {
        viewOverride = !SpatialGUIClient.getEffectiveFirstPersonMode();
    }

    public static boolean previewing() {
        return previewing;
    }

    public static void setPreviewing(boolean bool) {
        previewing = bool;
    }
}
