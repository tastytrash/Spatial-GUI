package org.tastytrash.spatialGUI.editor;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;
import org.tastytrash.spatialGUI.util.MouseHandlerUtil;

public class EditorMode {
    public record EditorButton(String translationKey, boolean rightSide, Runnable action) {}

    public static final EditorButton[] BUTTONS = {
            new EditorButton("spatial-gui.editor.button.undo", false, EditorHistory::undo),
            new EditorButton("spatial-gui.editor.button.redo", false, EditorHistory::redo),
            new EditorButton("spatial-gui.editor.button.toggle_view", false, EditorMode::toggleView),
            new EditorButton("spatial-gui.editor.button.undo_all", true, EditorHistory::undoAll),
            new EditorButton("spatial-gui.editor.button.save_quit", true, () -> toggle(Minecraft.getInstance()))
    };

    private static Boolean viewOverride;
    private static boolean previewing;
    private static boolean mouseFree;

    public static String[] getHints() {
        boolean firstPerson = SpatialGUIClient.getEffectiveFirstPersonMode();
        List<String> hints = new ArrayList<>(List.of(
                "spatial-gui.editor.hint.esc",
                "spatial-gui.editor.hint.scroll",
                "spatial-gui.editor.hint.rmb",
                "spatial-gui.editor.hint.mmb",
                firstPerson ? "spatial-gui.editor.hint.v_third" : "spatial-gui.editor.hint.v_first",
                "spatial-gui.editor.hint.ctrl_z",
                "spatial-gui.editor.hint.ctrl_y",
                "spatial-gui.editor.hint.space"
        ));
        if (SpatialGUIRenderer.isCrosshairModeActive()) {
            hints.add(mouseFree ? "spatial-gui.editor.hint.tab_grab" : "spatial-gui.editor.hint.tab_free");
        }
        return hints.toArray(new String[0]);
    }

    public static void toggle(Minecraft client) {
        if (client.level == null) return;
        Screen screen = client.screen;
        if (screen instanceof EditorScreen) {
            screen.onClose();
        } else {
            SpatialGUIClient.showScreen(client, new EditorScreen());
        }
    }

    public static void begin() {
        viewOverride = null;
        previewing = false;
        mouseFree = false;
    }

    public static void end() {
        viewOverride = null;
        mouseFree = false;
    }

    public static Boolean viewOverride() {
        return viewOverride;
    }

    public static void toggleView() {
        viewOverride = !SpatialGUIClient.getEffectiveFirstPersonMode();
        mouseFree = false;
    }

    public static boolean previewing() {
        return previewing;
    }

    public static void setPreviewing(boolean bool) {
        previewing = bool;
    }

    public static boolean mouseFree() {
        return mouseFree;
    }

    public static boolean cursorFree() {
        return mouseFree || !SpatialGUIRenderer.isCrosshairModeActive();
    }

    public static void toggleMouseFree() {
        mouseFree = !mouseFree;
        MouseHandlerUtil.centerCursor();
    }

    public static void resetScreenDistance() {
        var config = ConfigManager.config();
        boolean fp = SpatialGUIClient.getEffectiveFirstPersonMode();
        if (fp) {
            config.firstPersonScreenDistance = 1.5;
        } else {
            config.screenDistance = 2.5;
        }
    }
}