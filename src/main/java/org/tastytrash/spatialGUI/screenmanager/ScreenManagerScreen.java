package org.tastytrash.spatialGUI.screenmanager;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
//? if >1.21.1 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import org.tastytrash.spatialGUI.config.SpatialGUIConfig;
import org.tastytrash.spatialGUI.util.DrawUtil;

public class ScreenManagerScreen extends Screen {
    private static final int MARGIN = DrawUtil.SCREEN_MARGIN;
    private static final int GAP = 8;
    private static final int TOP = MARGIN + DrawUtil.BUTTON_HEIGHT + GAP;
    private static final int HEADER = 18;
    private static final int SAVE_QUIT_WIDTH = 90;
    private static final String[] COLUMNS = {"unsorted", "enabled", "disabled"};

    private final Screen parent;
    private final Button saveQuitButton = DrawUtil.button(SAVE_QUIT_WIDTH);

    public ScreenManagerScreen(Screen parent) {
        super(Component.translatable("spatial-gui.screen_manager.title"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(SpatialGUIConfig.createScreen(parent));
    }

    @Override
    //? if >26.3 {
    /*public void extractRenderState(GuiGraphicsExtractor g) {
     *///?} else if >1.21.11 {
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        //?} else {
        /*public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
         *///?}
        DrawUtil.fillRect(g, 0, 0, width, height, 0xD0101010);

        drawTitle(g);
        drawColumns(g);

        DrawUtil.drawButton(
                saveQuitButton,
                Component.translatable("spatial-gui.editor.button.save_quit"),
                width - MARGIN - SAVE_QUIT_WIDTH,
                MARGIN,
                true, g
        );
    }

    private void drawColumns(GuiGraphicsExtractor g) {
        int columnWidth = (width - MARGIN * 2 - GAP * 2) / 3;
        int columnHeight = height - TOP - MARGIN;

        for (int i = 0; i < COLUMNS.length; i++) {
            int x = MARGIN + i * (columnWidth + GAP);
            DrawUtil.fillRect(g, x, TOP, x + columnWidth, TOP + columnHeight, 0xFF1E1E1E);
            DrawUtil.fillRect(g, x, TOP, x + columnWidth, TOP + HEADER, 0xFF2C2C2C);
            g.outline(x, TOP, columnWidth, columnHeight, 0xFF4A4A4A);
            DrawUtil.drawTranslatedText(g, "spatial-gui.screen_manager." + COLUMNS[i], x + 6, TOP + 5);
        }
    }

    public void drawTitle(Object g) {
        String title = Component.translatable("spatial-gui.screen_manager.title").getString();
        float titleScale = 1.5f;
        int titleWidth = (int) (Minecraft.getInstance().font.width(title) * titleScale);
        DrawUtil.pushScale(g, titleScale);
        DrawUtil.drawText(g, title, (int) (MARGIN / titleScale), (int) ((MARGIN + 3) / titleScale));
        DrawUtil.popScale(g);
        DrawUtil.fillRect(g, MARGIN, MARGIN + DrawUtil.BUTTON_HEIGHT - 2, MARGIN + titleWidth, MARGIN + DrawUtil.BUTTON_HEIGHT, 0xFF33AAFF);
    }

    //? if >1.21.1 {
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    //?} else {
    /*@Override
    public boolean mouseClicked(double x, double y, int button) {
    *///?}
        //? if >1.21.1 {
        int button = event.button();
        double x = event.x();
        double y = event.y();
        //?}
        if (button == InputConstants.MOUSE_BUTTON_LEFT && saveQuitButton.isMouseOver(x, y)) {
            onClose();
            return true;
        }
        //? if >1.21.1 {
        return super.mouseClicked(event, doubleClick);
        //?} else {
        /*return super.mouseClicked(x, y, button);
         *///?}
    }
}