package org.tastytrash.spatialGUI.editor;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.config.SpatialGUIConfig;
import org.tastytrash.spatialGUI.util.EditorUtil;

final class EditorHelp {
    private static final int PANEL_WIDTH = 320;
    private static final int PADDING = 8;
    private static final int LINE_HEIGHT = 10;
    private static final int TITLE_HEIGHT = 16;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_GAP = 4;

    private static final int INTRO_LINES = 10;
    private static final int HELP_LINES = 14;

    private static final Button helpButton = EditorUtil.button(EditorUtil.BUTTON_HEIGHT);
    private static final Button closeButton = EditorUtil.button(BUTTON_WIDTH);
    private static final Button switchButton = EditorUtil.button(BUTTON_WIDTH);

    private static boolean open;
    private static boolean intro;

    private EditorHelp() {}

    static void begin() {
        intro = !SpatialGUI.config.editorIntroSeen;
        open = intro;
        if (intro) {
            SpatialGUI.config.editorIntroSeen = true;
            SpatialGUIConfig.HANDLER.save();
        }
    }

    static boolean isOpen() {
        return open;
    }

    static boolean clickHelpButton(double cursorX, double cursorY, int mouseButton) {
        if (open || mouseButton != InputConstants.MOUSE_BUTTON_LEFT || !helpButton.isMouseOver(cursorX, cursorY)) return false;
        EditorDropdown.close();
        intro = false;
        open = true;
        return true;
    }

    static void click(double cursorX, double cursorY) {
        if (closeButton.isMouseOver(cursorX, cursorY)) open = false;
        else if (switchButton.isMouseOver(cursorX, cursorY)) intro = !intro;
    }

    static boolean keyPressed(int key) {
        if (key == InputConstants.KEY_ESCAPE || key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER) {
            open = false;
        }
        return true;
    }

    static void draw(Object g, int guiWidth, int guiHeight) {
        int helpX = guiWidth - EditorUtil.SCREEN_MARGIN - EditorDropdown.BUTTON_WIDTH - BUTTON_GAP - EditorUtil.BUTTON_HEIGHT;
        EditorUtil.drawButton(helpButton, Component.literal("?"), helpX, EditorUtil.SCREEN_MARGIN, EditorMode.cursorFree(), g);
        if (!open) return;

        if (!EditorMode.mouseFree()) EditorMode.toggleMouseFree();

        String prefix = intro ? "spatial-gui.editor.help.intro." : "spatial-gui.editor.help.";
        int lines = intro ? INTRO_LINES : HELP_LINES;

        int height = PADDING + TITLE_HEIGHT + lines * LINE_HEIGHT + PADDING + EditorUtil.BUTTON_HEIGHT + PADDING;
        int x = (guiWidth - PANEL_WIDTH) / 2;
        int y = Math.max(EditorUtil.SCREEN_MARGIN, (guiHeight - height) / 2);

        EditorUtil.fillRect(g, 0, 0, guiWidth, guiHeight, 0xB0000000);
        EditorUtil.fillRect(g, x - 2, y - 2, x + PANEL_WIDTH + 2, y + height + 2, 0xFF33AAFF);
        EditorUtil.fillRect(g, x, y, x + PANEL_WIDTH, y + height, 0xFF101010);

        int textY = y + PADDING;
        EditorUtil.drawTranslatedText(g, prefix + "title", x + PADDING, textY);
        textY += TITLE_HEIGHT;
        for (int i = 1; i <= lines; i++) {
            EditorUtil.drawTranslatedText(g, prefix + i, x + PADDING, textY);
            textY += LINE_HEIGHT;
        }
        textY += PADDING;

        boolean usable = EditorMode.cursorFree();
        int buttonsX = x + (PANEL_WIDTH - 2 * BUTTON_WIDTH - BUTTON_GAP) / 2;
        Component switchLabel = Component.translatable(intro ? "spatial-gui.editor.help.show_instructions" : "spatial-gui.editor.help.show_intro");
        Component closeLabel = Component.translatable(intro ? "spatial-gui.editor.help.got_it" : "spatial-gui.editor.help.close");
        EditorUtil.drawButton(switchButton, switchLabel, buttonsX, textY, usable, g);
        EditorUtil.drawButton(closeButton, closeLabel, buttonsX + BUTTON_WIDTH + BUTTON_GAP, textY, usable, g);
    }
}