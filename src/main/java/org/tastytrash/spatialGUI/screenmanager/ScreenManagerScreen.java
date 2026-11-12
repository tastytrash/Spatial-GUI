package org.tastytrash.spatialGUI.screenmanager;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
//? if >1.21.1 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.tastytrash.spatialGUI.config.SpatialGUIConfig;
import org.tastytrash.spatialGUI.util.DrawUtil;

import java.util.List;

public class ScreenManagerScreen extends Screen {
    private static final int MARGIN = DrawUtil.SCREEN_MARGIN;
    private static final int GAP = 8;
    private static final int VERTICAL_MARGIN = MARGIN + DrawUtil.BUTTON_HEIGHT + GAP;
    private static final int HEADER = 18;
    private static final int ROW_HEIGHT = 20;
    private static final int SAVE_QUIT_WIDTH = 80;
    private static final int MOVE_WIDTH = 30;
    private static final int ADVANCED_WIDTH = 90;
    private static final int SORT_WIDTH = 70;
    private static final String[] COLUMNS = {"unsorted", "enabled", "disabled"};

    private final Screen parent;
    private final ScreenManagerData data = new ScreenManagerData();
    private final int[] scrollOffsets = new int[ScreenManagerData.COLUMN_COUNT];

    private final Button saveQuitButton = DrawUtil.button(SAVE_QUIT_WIDTH);
    private final Button advancedModeButton = DrawUtil.button(ADVANCED_WIDTH);
    private final Button sortButton = DrawUtil.button(SORT_WIDTH);
    private final Button moveLeftButton = DrawUtil.button(MOVE_WIDTH);
    private final Button moveRightButton = DrawUtil.button(MOVE_WIDTH);

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
        int mouseX = (int) DrawUtil.cursorX();
        int mouseY = (int) DrawUtil.cursorY();
     *///?} else if >1.21.11 {
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        //?} else {
        /*public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
         *///?}
        DrawUtil.fillRect(g, 0, 0, width, height, 0xD0101010);

        drawTitle(g);
        drawColumns(g);
        drawTopButtons(g);

        int footerY = height - MARGIN - DrawUtil.BUTTON_HEIGHT;
        DrawUtil.drawButton(moveLeftButton, Component.literal("<"), width / 2 - MOVE_WIDTH - 4, footerY, data.canMove(-1), g);
        DrawUtil.drawButton(moveRightButton, Component.literal(">"), width / 2 + 4, footerY, data.canMove(1), g);

        drawIdTooltip(g, mouseX, mouseY);
    }

    private void drawTitle(Object g) {
        String title = Component.translatable("spatial-gui.screen_manager.title").getString();
        float titleScale = 1.5f;
        int titleWidth = (int) (Minecraft.getInstance().font.width(title) * titleScale);
        DrawUtil.pushScale(g, titleScale);
        DrawUtil.drawText(g, title, (int) (MARGIN / titleScale), (int) ((MARGIN + 3) / titleScale));
        DrawUtil.popScale(g);
        DrawUtil.fillRect(g, MARGIN, MARGIN + DrawUtil.BUTTON_HEIGHT - 2, MARGIN + titleWidth, MARGIN + DrawUtil.BUTTON_HEIGHT, 0xFF33AAFF);
    }

    private void drawTopButtons(Object g) {
        int right = width - MARGIN;
        right = drawTopButton(g, saveQuitButton, right, Component.translatable("spatial-gui.editor.button.save_quit"));
        right = drawTopButton(g, advancedModeButton, right, Component.translatable(
                "spatial-gui.screen_manager.advanced_mode",
                Component.translatable(data.isAdvancedMode() ? "options.on" : "options.off")));
        drawTopButton(g, sortButton, right, Component.translatable(
                "spatial-gui.screen_manager.sort",
                Component.translatable(data.isSortById() ? "spatial-gui.screen_manager.sort.id" : "spatial-gui.screen_manager.sort.name")));
    }

    private int drawTopButton(Object g, Button button, int right, Component label) {
        int x = right - button.getWidth();
        DrawUtil.drawButton(button, label, x, MARGIN, true, g);
        return x - 4;
    }

    private void drawColumns(GuiGraphicsExtractor g) {
        int columnWidth = columnWidth();
        for (int i = 0; i < COLUMNS.length; i++) {
            int x = columnX(i);
            List<String> ids = data.ids(i);
            DrawUtil.fillRect(g, x, VERTICAL_MARGIN, x + columnWidth, VERTICAL_MARGIN + columnHeight(), 0xFF1E1E1E);
            DrawUtil.fillRect(g, x, VERTICAL_MARGIN, x + columnWidth, VERTICAL_MARGIN + HEADER, 0xFF2C2C2C);
            String header = Component.translatable("spatial-gui.screen_manager." + COLUMNS[i]).getString();
            DrawUtil.drawText(g, header + " (" + ids.size() + ")", x + 6, VERTICAL_MARGIN + 5);

            for (int row = 0; row < visibleRows() && row + scrollOffsets[i] < ids.size(); row++) {
                String id = ids.get(row + scrollOffsets[i]);
                int y = rowY(row);
                if (data.isSelected(i, id)) {
                    DrawUtil.fillRect(g, x + 1, y, x + columnWidth - 1, y + ROW_HEIGHT, 0x4033AAFF);
                }
                DrawUtil.drawText(g, DrawUtil.fitText(data.displayName(id), columnWidth - 12), x + 6, y + 6);
            }
            g.outline(x, VERTICAL_MARGIN, columnWidth, columnHeight(), 0xFF4A4A4A);
        }
    }

    private void drawIdTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (!data.isAdvancedMode()) return;
        String id = idAt(mouseX, mouseY);
        if (id == null) return;

        String text = DrawUtil.fitText(id, width - 8);
        int boxWidth = Minecraft.getInstance().font.width(text) + 8;
        int boxHeight = 16;
        int x = Mth.clamp(mouseX + 8, 0, width - boxWidth);
        int y = Mth.clamp(mouseY + 8, 0, height - boxHeight);

        DrawUtil.fillRect(g, x, y, x + boxWidth, y + boxHeight, 0xF0101010);
        g.outline(x, y, boxWidth, boxHeight, 0xFF33AAFF);
        DrawUtil.drawText(g, text, x + 4, y + 4);
    }

    private int columnWidth() {
        return (width - MARGIN * 2 - GAP * 2) / 3;
    }

    private int columnX(int column) {
        return MARGIN + column * (columnWidth() + GAP);
    }

    private int columnHeight() {
        return height - 2 * VERTICAL_MARGIN;
    }

    private int rowY(int row) {
        return VERTICAL_MARGIN + HEADER + row * ROW_HEIGHT;
    }

    private int visibleRows() {
        return (columnHeight() - HEADER - 1) / ROW_HEIGHT;
    }

    private int columnAt(double mouseX) {
        for (int i = 0; i < COLUMNS.length; i++) {
            if (mouseX >= columnX(i) && mouseX < columnX(i) + columnWidth()) return i;
        }
        return -1;
    }

    private String idAt(double mouseX, double mouseY) {
        int column = columnAt(mouseX);
        double relative = mouseY - rowY(0);
        if (column < 0 || relative < 0) return null;
        int row = (int) (relative / ROW_HEIGHT);
        int index = row + scrollOffsets[column];
        List<String> ids = data.ids(column);
        if (row >= visibleRows() || index >= ids.size()) return null;
        return ids.get(index);
    }

    private boolean selectRow(double mouseX, double mouseY) {
        String id = idAt(mouseX, mouseY);
        if (id == null) return false;
        data.select(columnAt(mouseX), id);
        return true;
    }

    private void move(int direction) {
        data.moveSelected(direction);
        for (int i = 0; i < scrollOffsets.length; i++) clampScroll(i);
        scrollToSelected();
    }

    private void scrollToSelected() {
        int column = data.getSelectedColumn();
        String id = data.getSelectedId();
        if (column < 0 || id == null) return;

        int index = data.ids(column).indexOf(id);
        if (index < 0) return;

        int rows = Math.max(1, visibleRows());
        scrollOffsets[column] = Mth.clamp(scrollOffsets[column], index - rows + 1, index);
        clampScroll(column);
    }

    private void clampScroll(int column) {
        int max = Math.max(0, data.ids(column).size() - visibleRows());
        scrollOffsets[column] = Mth.clamp(scrollOffsets[column], 0, max);
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
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            if (saveQuitButton.isMouseOver(x, y)) {
                data.save();
                onClose();
                return true;
            }
            if (advancedModeButton.isMouseOver(x, y)) {
                data.toggleAdvancedMode();
                return true;
            }
            if (sortButton.isMouseOver(x, y)) {
                data.toggleSortById();
                return true;
            }
            if (moveLeftButton.isMouseOver(x, y)) {
                move(-1);
                return true;
            }
            if (moveRightButton.isMouseOver(x, y)) {
                move(1);
                return true;
            }
            if (selectRow(x, y)) return true;
        }
        //? if >1.21.1 {
        return super.mouseClicked(event, doubleClick);
        //?} else {
        /*return super.mouseClicked(x, y, button);
         *///?}
    }

    //? if >1.20.1 {
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    //?} else {
    /*@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
    *///?}
        int column = columnAt(mouseX);
        if (column < 0) return false;
        scrollOffsets[column] += (int) -Math.signum(scrollY);
        clampScroll(column);
        return true;
    }

    //? if >1.21.1 {
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (copySelected(event.key(), event.hasControlDown())) return true;
        return super.keyPressed(event);
    }
    //?} else {
    /*@Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (copySelected(keyCode, hasControlDown())) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    *///?}

    private boolean copySelected(int keyCode, boolean control) {
        String id = data.getSelectedId();
        if (keyCode != InputConstants.KEY_C || !control || id == null) return false;
        Minecraft.getInstance().keyboardHandler.setClipboard(id);
        return true;
    }
}