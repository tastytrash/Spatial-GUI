package org.tastytrash.spatialGUI.editor;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIKeybinds;
import org.tastytrash.spatialGUI.config.SpatialGUIConfig;
import org.tastytrash.spatialGUI.util.DrawUtil;

final class EditorHelp {
    private static final int PANEL_WIDTH = 320;
    private static final int PADDING = 8;
    private static final int LINE_HEIGHT = 10;
    private static final int TITLE_HEIGHT = 16;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_GAP = 4;

    private enum Page {
        INTRO("spatial-gui.editor.help.intro.", 10, "spatial-gui.editor.help.show_intro"),
        INSTRUCTIONS("spatial-gui.editor.help.", 14, "spatial-gui.editor.help.show_instructions"),
        PROFILES("spatial-gui.editor.help.profile.", 5, "spatial-gui.editor.help.show_profiles");

        final String prefix;
        final int lines;
        final String label;

        Page(String prefix, int lines, String label) {
            this.prefix = prefix;
            this.lines = lines;
            this.label = label;
        }

        Page next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private static final Button helpButton = DrawUtil.button(DrawUtil.BUTTON_HEIGHT);
    private static final Button closeButton = DrawUtil.button(BUTTON_WIDTH);
    private static final Button switchButton = DrawUtil.button(BUTTON_WIDTH);

    private static boolean open;
    private static boolean profileExitPrompt;
    private static Page page = Page.INSTRUCTIONS;

    private EditorHelp() {}

    static void begin() {
        profileExitPrompt = false;
        open = !SpatialGUI.config.editorIntroSeen;
        page = open ? Page.INTRO : Page.INSTRUCTIONS;
        if (open) {
            SpatialGUI.config.editorIntroSeen = true;
            SpatialGUIConfig.HANDLER.save();
        }
    }

    static boolean isOpen() {
        return open;
    }

    static boolean shouldShowProfileTip() {
        return !SpatialGUI.config.profilePickerTipSeen && EditorProfiles.count() > 1;
    }

    static void showProfileTip() {
        EditorDropdown.close();
        profileExitPrompt = true;
        page = Page.PROFILES;
        open = true;
    }

    private static void dismiss() {
        open = false;
        if (!profileExitPrompt) return;
        profileExitPrompt = false;
        SpatialGUI.config.profilePickerTipSeen = true;
        SpatialGUIConfig.HANDLER.save();
        EditorScreen.closeEditor();
    }

    static boolean clickHelpButton(double cursorX, double cursorY, int mouseButton) {
        if (open || mouseButton != InputConstants.MOUSE_BUTTON_LEFT || !helpButton.isMouseOver(cursorX, cursorY)) return false;
        EditorDropdown.close();
        page = Page.INSTRUCTIONS;
        open = true;
        return true;
    }

    static void click(double cursorX, double cursorY) {
        if (closeButton.isMouseOver(cursorX, cursorY)) dismiss();
        else if (!profileExitPrompt && switchButton.isMouseOver(cursorX, cursorY)) page = page.next();
    }

    static boolean keyPressed(int key) {
        if (key == InputConstants.KEY_ESCAPE || key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER) {
            dismiss();
        }
        return true;
    }

    static void draw(Object g, int guiWidth, int guiHeight) {
        int helpX = guiWidth - DrawUtil.SCREEN_MARGIN - EditorDropdown.BUTTON_WIDTH - BUTTON_GAP - DrawUtil.BUTTON_HEIGHT;
        DrawUtil.drawButton(helpButton, Component.literal("?"), helpX, DrawUtil.SCREEN_MARGIN, EditorMode.cursorFree(), g);
        if (!open) return;

        DrawUtil.pushLayer(g);

        if (!EditorMode.mouseFree()) EditorMode.toggleMouseFree();

        Component pickerKey = SpatialGUIKeybinds.profilePickerKey();
        String keyName = pickerKey != null ? pickerKey.getString() : "";
        String prefix = page == Page.PROFILES && pickerKey == null ? "spatial-gui.editor.help.profile.unbound." : page.prefix;
        int lines = page.lines;

        int height = PADDING + TITLE_HEIGHT + lines * LINE_HEIGHT + PADDING + DrawUtil.BUTTON_HEIGHT + PADDING;
        int x = (guiWidth - PANEL_WIDTH) / 2;
        int y = Math.max(DrawUtil.SCREEN_MARGIN, (guiHeight - height) / 2);

        DrawUtil.fillRect(g, 0, 0, guiWidth, guiHeight, 0xB0000000);
        DrawUtil.fillRect(g, x - 2, y - 2, x + PANEL_WIDTH + 2, y + height + 2, 0xFF33AAFF);
        DrawUtil.fillRect(g, x, y, x + PANEL_WIDTH, y + height, 0xFF101010);

        int textY = y + PADDING;
        drawLine(g, prefix + "title", keyName, x + PADDING, textY);
        textY += TITLE_HEIGHT;
        for (int i = 1; i <= lines; i++) {
            drawLine(g, prefix + i, keyName, x + PADDING, textY);
            textY += LINE_HEIGHT;
        }
        textY += PADDING;

        boolean usable = EditorMode.cursorFree();
        if (profileExitPrompt) {
            Component doneLabel = Component.translatable("spatial-gui.editor.button.save_quit");
            DrawUtil.drawButton(closeButton, doneLabel, x + (PANEL_WIDTH - BUTTON_WIDTH) / 2, textY, usable, g);
        } else {
            int buttonsX = x + (PANEL_WIDTH - 2 * BUTTON_WIDTH - BUTTON_GAP) / 2;
            Component switchLabel = Component.translatable(page.next().label);
            Component closeLabel = Component.translatable(page == Page.INTRO ? "spatial-gui.editor.help.got_it" : "spatial-gui.editor.help.close");
            DrawUtil.drawButton(switchButton, switchLabel, buttonsX, textY, usable, g);
            DrawUtil.drawButton(closeButton, closeLabel, buttonsX + BUTTON_WIDTH + BUTTON_GAP, textY, usable, g);
        }

        DrawUtil.popLayer(g);
    }

    private static void drawLine(Object g, String key, String keyName, int x, int y) {
        DrawUtil.drawText(g, Component.translatable(key, keyName).getString(), x, y);
    }
}