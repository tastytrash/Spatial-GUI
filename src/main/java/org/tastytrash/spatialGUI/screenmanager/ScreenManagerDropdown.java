package org.tastytrash.spatialGUI.screenmanager;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.editor.EditorProfiles;
import org.tastytrash.spatialGUI.util.DrawUtil;

final class ScreenManagerDropdown {
    static final int BUTTON_WIDTH = 140;

    private static final Button header = DrawUtil.button(BUTTON_WIDTH);
    private static final Button option = DrawUtil.button(BUTTON_WIDTH);

    private static boolean open;
    private static int xPos;

    private ScreenManagerDropdown() {}

    static void close() {
        open = false;
    }

    private static int currentIndex(String screenId) {
        String assigned = ConfigManager.profiles().getScreenProfile(screenId);
        int index = assigned == null ? -1 : ConfigManager.profiles().names().indexOf(assigned);
        return index < 0 ? EditorProfiles.getDefaultSlot() : index;
    }

    private static void apply(String screenId, int index) {
        String profile = index == EditorProfiles.getDefaultSlot() ? null : EditorProfiles.name(index);
        ConfigManager.profiles().setScreenProfile(screenId, profile);
        ConfigManager.saveProfiles();
        close();
    }

    private static String label(int index) {
        return "(" + (index + 1) + ") " + EditorProfiles.label(index);
    }

    private static int rowAt(double cursorX, double cursorY, int topY, int headerY) {
        if (cursorX < xPos || cursorX >= xPos + BUTTON_WIDTH) return -2;
        if (cursorY < topY || cursorY >= headerY) return -2;
        return (int) Math.floor((cursorY - topY) / DrawUtil.BUTTON_HEIGHT);
    }

    static void draw(Object g, int guiWidth, int guiHeight, ScreenManagerData data) {
        String selectedId = data.getSelectedId();
        if (selectedId == null) {
            close();
            return;
        }

        int headerY = guiHeight - DrawUtil.SCREEN_MARGIN - DrawUtil.BUTTON_HEIGHT;
        int currentProfile = currentIndex(selectedId);
        int count = EditorProfiles.count();
        int topY = headerY - count * DrawUtil.BUTTON_HEIGHT;
        xPos = guiWidth - DrawUtil.SCREEN_MARGIN - BUTTON_WIDTH;

        DrawUtil.drawButton(header, Component.literal(label(currentProfile) + (open ? " ▼" : " ▲")), xPos, headerY, true, g);
        if (!open) return;

        for (int i = 0; i < count; i++) {
            int rowY = topY + i * DrawUtil.BUTTON_HEIGHT;
            DrawUtil.drawButton(option, Component.literal(label(i)), xPos, rowY, i != currentProfile, g);
        }
    }

    static boolean click(double cursorX, double cursorY, int mouseButton, int guiHeight, ScreenManagerData data) {
        String selectedId = data.getSelectedId();
        if (selectedId == null) {
            close();
            return false;
        }

        boolean left = mouseButton == InputConstants.MOUSE_BUTTON_LEFT;
        if (left && header.isMouseOver(cursorX, cursorY)) {
            open = !open;
            return true;
        }
        if (!open) return false;

        int headerY = guiHeight - DrawUtil.SCREEN_MARGIN - DrawUtil.BUTTON_HEIGHT;
        int count = EditorProfiles.count();
        int topY = headerY - count * DrawUtil.BUTTON_HEIGHT;
        int row = rowAt(cursorX, cursorY, topY, headerY);
        if (row >= 0 && row < count) {
            if (left) {
                apply(selectedId, row);
                open = false;
            }
            return true;
        }

        open = false;
        return true;
    }
}