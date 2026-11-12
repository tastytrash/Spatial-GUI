package org.tastytrash.spatialGUI.screenmanager;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
//? if >1.21.1 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.tastytrash.spatialGUI.config.SpatialGUIConfig;
import org.tastytrash.spatialGUI.util.DrawUtil;
import org.tastytrash.spatialGUI.util.UiColors;

import java.util.List;
import java.util.Locale;

public class ScreenManagerScreen extends Screen {
    private static final int MARGIN = DrawUtil.SCREEN_MARGIN;
    private static final int GAP = 8;
    private static final int VERTICAL_MARGIN = MARGIN + DrawUtil.BUTTON_HEIGHT + GAP;
    private static final int HEADER = 18;
    private static final int ROW_HEIGHT = 20;
    private static final int ADVANCED_WIDTH = 90;
    private static final int CLOSE_WIDTH = 50;
    private static final int HELP_WIDTH = 20;
    private static final int MOVE_WIDTH = 30;
    private static final int SORT_WIDTH = 70;
    private static final int SEARCH_WIDTH = 150;
    private static final String[] COLUMNS = {"unsorted", "enabled", "disabled"};

    private final Screen parent;
    private final ScreenManagerData data = new ScreenManagerData();
    private final int[] scrollOffsets = new int[ScreenManagerData.COLUMN_COUNT];
    private final EditBox searchBox = new EditBox(Minecraft.getInstance().font, 0, 0, SEARCH_WIDTH, DrawUtil.BUTTON_HEIGHT, Component.empty());

    private final Button advancedModeButton = DrawUtil.button(ADVANCED_WIDTH);
    private final Button closeButton = DrawUtil.button(CLOSE_WIDTH);
    private final Button helpButton = DrawUtil.button(HELP_WIDTH);
    private final Button moveLeftButton = DrawUtil.button(MOVE_WIDTH);
    private final Button moveRightButton = DrawUtil.button(MOVE_WIDTH);
    private final Button sortButton = DrawUtil.button(SORT_WIDTH);

    public ScreenManagerScreen(Screen parent) {
        super(Component.translatable("spatial-gui.screen_manager.title"));
        this.parent = parent;
        searchBox.setMaxLength(50);
        searchBox.setHint(Component.translatable("spatial-gui.screen_manager.search"));
    }

    @Override
    public void onClose() {
        ScreenManagerDropdown.close();
        data.save();
        Minecraft mc = Minecraft.getInstance();
        Screen screen = SpatialGUIConfig.createScreen(parent);
        mc.setScreen(screen);
        if (screen instanceof YACLScreen yacl) yacl.tabNavigationBar.selectTab(1, false);
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
        DrawUtil.fillRect(g, 0, 0, width, height, UiColors.NEAR_BLACK_80);

        drawTitle(g);
        drawColumns(g);
        drawTopButtons(g);
        drawSearchBox(g);

        int footerY = height - MARGIN - DrawUtil.BUTTON_HEIGHT;
        DrawUtil.drawButton(moveLeftButton, Component.literal("<"), width / 2 - MOVE_WIDTH - 4, footerY, data.canMove(-1), g);
        DrawUtil.drawButton(moveRightButton, Component.literal(">"), width / 2 + 4, footerY, data.canMove(1), g);

        drawIdTooltip(g, mouseX, mouseY);
        ScreenManagerHelp.draw(g, width, height);
        ScreenManagerDropdown.draw(g, width, height, data);
    }

    private void drawTitle(Object g) {
        String title = Component.translatable("spatial-gui.screen_manager.title").getString();
        float titleScale = 1.5f;
        int titleWidth = (int) (Minecraft.getInstance().font.width(title) * titleScale);
        DrawUtil.pushScale(g, titleScale);
        DrawUtil.drawText(g, title, (int) (MARGIN / titleScale), (int) ((MARGIN + 3) / titleScale));
        DrawUtil.popScale(g);
        DrawUtil.fillRect(g, MARGIN, MARGIN + DrawUtil.BUTTON_HEIGHT - 2, MARGIN + titleWidth, MARGIN + DrawUtil.BUTTON_HEIGHT, UiColors.SKY_BLUE);
    }

    private void drawTopButtons(Object g) {
        int right = width - MARGIN;
        right = drawTopButton(g, closeButton, right, Component.translatable("spatial-gui.screen_manager.close"));
        right = drawTopButton(g, advancedModeButton, right, Component.translatable(
                "spatial-gui.screen_manager.advanced_mode",
                Component.translatable(data.isAdvancedMode() ? "options.on" : "options.off")));
        right = drawTopButton(g, sortButton, right, Component.translatable(
                "spatial-gui.screen_manager.sort",
                Component.translatable(data.isSortById() ? "spatial-gui.screen_manager.sort.id" : "spatial-gui.screen_manager.sort.name")));
        drawTopButton(g, helpButton, right, Component.literal("?"));
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
            clampScroll(i);
            List<String> ids = filterIds(data.ids(i));
            DrawUtil.fillRect(g, x, VERTICAL_MARGIN, x + columnWidth, VERTICAL_MARGIN + columnHeight(), UiColors.DARKEST_GRAY);
            DrawUtil.fillRect(g, x, VERTICAL_MARGIN, x + columnWidth, VERTICAL_MARGIN + HEADER, UiColors.DARKER_GRAY);
            String header = Component.translatable("spatial-gui.screen_manager." + COLUMNS[i]).getString();
            DrawUtil.drawText(g, header + " (" + ids.size() + ")", x + 6, VERTICAL_MARGIN + 5);

            for (int row = 0; row < visibleRows() && row + scrollOffsets[i] < ids.size(); row++) {
                String id = ids.get(row + scrollOffsets[i]);
                int y = rowY(row);
                if (data.isSelected(i, id)) {
                    DrawUtil.fillRect(g, x + 1, y, x + columnWidth - 1, y + ROW_HEIGHT, UiColors.SKY_BLUE_25);
                }
                DrawUtil.drawText(g, DrawUtil.fitText(data.displayName(id), columnWidth - 12), x + 6, y + 6);
            }
            drawScrollbar(g, x, i);
            g.outline(x, VERTICAL_MARGIN, columnWidth, columnHeight(), UiColors.DARK_GRAY);
        }
    }

    private void drawScrollbar(GuiGraphicsExtractor g, int columnX, int column) {
        int totalRows = data.ids(column).size();
        int visible = visibleRows();
        int scrollableArea = columnHeight() - HEADER;
        
        if (totalRows <= visible) return;
        
        int barHeight = Math.max(4, (scrollableArea * visible / totalRows));
        int barY = VERTICAL_MARGIN + HEADER + (int) ((float) scrollOffsets[column] / (totalRows - visible) * (scrollableArea - barHeight));
        
        DrawUtil.fillRect(g, columnX + columnWidth() - 4, barY, columnX + columnWidth(), barY + barHeight, UiColors.LIGHT_GRAY);
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

        DrawUtil.pushLayer(g);
        DrawUtil.fillRect(g, x, y, x + boxWidth, y + boxHeight, UiColors.NEAR_BLACK_95);
        g.outline(x, y, boxWidth, boxHeight, UiColors.SKY_BLUE);
        DrawUtil.drawText(g, text, x + 4, y + 4);
        DrawUtil.popLayer(g);
    }

    private void drawSearchBox(GuiGraphicsExtractor g) {
        int right = width / 2 - MOVE_WIDTH - 4 - GAP;
        searchBox.setWidth(Mth.clamp(right - MARGIN, 40, SEARCH_WIDTH));
        searchBox.setPosition(MARGIN, height - MARGIN - DrawUtil.BUTTON_HEIGHT);
        DrawUtil.renderWidget(searchBox, g);
    }

    private List<String> filterIds(List<String> ids) {
        String query = searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        return ids.stream().filter(id -> id.toLowerCase(Locale.ROOT).contains(query)).toList();
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
        int row = (int) ((mouseY - rowY(0)) / ROW_HEIGHT);
        if (column < 0 || row < 0 || row >= visibleRows()) return null;

        List<String> ids = filterIds(data.ids(column));
        int index = row + scrollOffsets[column];
        return index < ids.size() ? ids.get(index) : null;
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
        if (column < 0) return;

        int index = filterIds(data.ids(column)).indexOf(data.getSelectedId());
        if (index < 0) return;

        int rows = Math.max(1, visibleRows());
        scrollOffsets[column] = Mth.clamp(scrollOffsets[column], index - rows + 1, index);
        clampScroll(column);
    }

    private void clampScroll(int column) {
        List<String> ids = filterIds(data.ids(column));
        int max = Math.max(0, ids.size() - visibleRows());
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
        if (ScreenManagerHelp.isOpen()) {
            if (button == InputConstants.MOUSE_BUTTON_LEFT) ScreenManagerHelp.click(x, y);
            return true;
        }
        if (ScreenManagerDropdown.click(x, y, button, height, data)) return true;
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            if (searchBox.isMouseOver(x, y)) {
                searchBox.setFocused(true);
                return true;
            } else {
                searchBox.setFocused(false);
            }
            if (closeButton.isMouseOver(x, y)) {
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
            if (helpButton.isMouseOver(x, y)) {
                ScreenManagerHelp.open();
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
        if (ScreenManagerHelp.isOpen()) return true;
        int column = columnAt(mouseX);
        if (column < 0) return false;
        List<String> ids = filterIds(data.ids(column));
        if (ids.isEmpty()) return false;
        scrollOffsets[column] += (int) -Math.signum(scrollY);
        clampScroll(column);
        return true;
    }

    //? if >1.21.1 {
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (ScreenManagerHelp.isOpen()) return ScreenManagerHelp.keyPressed(event.key());
        if (searchBox.isFocused()) {
            if (event.key() == InputConstants.KEY_ESCAPE) searchBox.setFocused(false);
            else searchBox.keyPressed(event);
            return true;
        }
        if (copySelected(event.key(), event.hasControlDown())) return true;
        if (moveByKey(event.key())) return true;
        return super.keyPressed(event);
    }
    //?} else {
    /*@Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ScreenManagerHelp.isOpen()) return ScreenManagerHelp.keyPressed(keyCode);
        if (searchBox.isFocused()) {
            if (keyCode == InputConstants.KEY_ESCAPE) searchBox.setFocused(false);
            else searchBox.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        if (copySelected(keyCode, hasControlDown())) return true;
        if (moveByKey(keyCode)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    *///?}

    //? if >1.21.1 {
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (ScreenManagerHelp.isOpen()) return true;
        if (searchBox.isFocused()) {
            searchBox.charTyped(event);
            return true;
        }
        return super.charTyped(event);
    }
    //?} else {
    /*@Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (ScreenManagerHelp.isOpen()) return true;
        if (searchBox.isFocused()) {
            searchBox.charTyped(codePoint, modifiers);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
    *///?}

    private boolean copySelected(int keyCode, boolean control) {
        String id = data.getSelectedId();
        if (keyCode != InputConstants.KEY_C || !control || id == null) return false;
        Minecraft.getInstance().keyboardHandler.setClipboard(id);
        return true;
    }

    private boolean moveByKey(int keyCode) {
        if (keyCode == InputConstants.KEY_LEFT || keyCode == InputConstants.KEY_RIGHT) {
            int direction = keyCode == InputConstants.KEY_LEFT ? -1 : 1;
            if (!data.canMove(direction)) return false;
            move(direction);
            return true;
        }
        if (keyCode == InputConstants.KEY_UP || keyCode == InputConstants.KEY_DOWN) {
            int direction = keyCode == InputConstants.KEY_UP ? -1 : 1;
            if (!data.moveSelection(direction)) return false;
            scrollToSelected();
            return true;
        }
        return false;
    }
}