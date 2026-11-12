package org.tastytrash.spatialGUI.screenmanager;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.tastytrash.spatialGUI.util.DrawUtil;
import org.tastytrash.spatialGUI.util.UiColors;

final class ScreenManagerHelp {
    private static final int PANEL_WIDTH = 320;
    private static final int PADDING = 8;
    private static final int LINE_HEIGHT = 10;
    private static final int TITLE_HEIGHT = 16;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_GAP = 4;

    private enum Page {
        BASICS("spatial-gui.screen_manager.help.basics.", 8, "spatial-gui.screen_manager.help.show_basics"),
        SHORTCUTS("spatial-gui.screen_manager.help.shortcuts.", 6, "spatial-gui.screen_manager.help.show_shortcuts");

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

    private static final Button closeButton = DrawUtil.button(BUTTON_WIDTH);
    private static final Button switchButton = DrawUtil.button(BUTTON_WIDTH);

    private static boolean open;
    private static Page page = Page.BASICS;

    private ScreenManagerHelp() {}

    static boolean isOpen() {
        return open;
    }

    static void open() {
        page = Page.BASICS;
        open = true;
    }

    static void click(double x, double y) {
        if (closeButton.isMouseOver(x, y)) open = false;
        else if (switchButton.isMouseOver(x, y)) page = page.next();
    }

    static boolean keyPressed(int key) {
        if (key == InputConstants.KEY_ESCAPE || key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER) {
            open = false;
        }
        return true;
    }

    static void draw(Object g, int guiWidth, int guiHeight) {
        if (!open) return;

        DrawUtil.pushLayer(g);

        int height = PADDING + TITLE_HEIGHT + page.lines * LINE_HEIGHT + PADDING + DrawUtil.BUTTON_HEIGHT + PADDING;
        int x = (guiWidth - PANEL_WIDTH) / 2;
        int y = Math.max(DrawUtil.SCREEN_MARGIN, (guiHeight - height) / 2);

        DrawUtil.fillRect(g, 0, 0, guiWidth, guiHeight, UiColors.BLACK_70);
        DrawUtil.fillRect(g, x - 2, y - 2, x + PANEL_WIDTH + 2, y + height + 2, UiColors.SKY_BLUE);
        DrawUtil.fillRect(g, x, y, x + PANEL_WIDTH, y + height, UiColors.NEAR_BLACK);

        int textY = y + PADDING;
        DrawUtil.drawTranslatedText(g, page.prefix + "title", x + PADDING, textY);
        textY += TITLE_HEIGHT;
        for (int i = 1; i <= page.lines; i++) {
            DrawUtil.drawTranslatedText(g, page.prefix + i, x + PADDING, textY);
            textY += LINE_HEIGHT;
        }
        textY += PADDING;

        int buttonsX = x + (PANEL_WIDTH - 2 * BUTTON_WIDTH - BUTTON_GAP) / 2;
        DrawUtil.drawButton(switchButton, Component.translatable(page.next().label), buttonsX, textY, true, g);
        DrawUtil.drawButton(closeButton, Component.translatable("spatial-gui.screen_manager.close"), buttonsX + BUTTON_WIDTH + BUTTON_GAP, textY, true, g);

        DrawUtil.popLayer(g);
    }
}