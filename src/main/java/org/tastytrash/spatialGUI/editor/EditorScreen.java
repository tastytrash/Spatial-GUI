package org.tastytrash.spatialGUI.editor;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.util.Mth;
//? if >1.21.1 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
//?}
//? if >1.21.11 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}

//? if <1.21.11 {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.config.SpatialGUIConfig;
import org.tastytrash.spatialGUI.util.MouseHandlerUtil;

public class EditorScreen extends Screen {
    //? if >=1.21.11 {
    private static final Identifier INVENTORY_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/inventory.png");
    //?} else if >1.20.1 {
    /*private static final ResourceLocation INVENTORY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");
     *///?} else {
    /*private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation("textures/gui/container/inventory.png");
     *///?}

    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 4;
    private static final int SCREEN_MARGIN = 6;
    private static final Button[] widgets = new Button[EditorMode.BUTTONS.length];
    private static double pxPerGuiX = 1;
    private static double pxPerGuiY = 1;

    static {
        for (int i = 0; i < widgets.length; i++) {
            widgets[i] = Button.builder(Component.translatable(EditorMode.BUTTONS[i].translationKey()), b -> {})
                    .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();
        }
    }

    private static void drawButtons(Object g, Minecraft client) {
        var window = client.getWindow();
        int guiWidth = window.getGuiScaledWidth();
        int guiHeight = window.getGuiScaledHeight();
        pxPerGuiX = window.getScreenWidth() / (double) guiWidth;
        pxPerGuiY = window.getScreenHeight() / (double) guiHeight;
        int cursorX = (int) (MouseHandlerUtil.getPhysicalX(-1) / pxPerGuiX);
        int cursorY = (int) (MouseHandlerUtil.getPhysicalY(-1) / pxPerGuiY);

        int leftCount = 0, rightCount = 0;
        for (var button : EditorMode.BUTTONS) {
            if (button.rightSide()) rightCount++;
            else leftCount++;
        }

        int leftPlaced = 0, rightPlaced = 0;
        boolean usable = EditorMode.cursorFree();

        for (int i = 0; i < widgets.length; i++) {
            var button = EditorMode.BUTTONS[i];
            boolean right = button.rightSide();
            int count = right ? rightCount : leftCount;
            int row = right ? rightPlaced++ : leftPlaced++;
            int x = right ? guiWidth - SCREEN_MARGIN - BUTTON_WIDTH : SCREEN_MARGIN;
            int y = guiHeight - SCREEN_MARGIN - (count - row) * (BUTTON_HEIGHT + BUTTON_SPACING) + BUTTON_SPACING;

            Button widget = widgets[i];
            widget.setPosition(x, y);

            if (i == 0 || i == 3) {
                widget.active = usable && EditorHistory.canUndo();
            } else if (i == 1) {
                widget.active = usable && EditorHistory.canRedo();
            } else {
                widget.active = usable;
            }

            //? if >1.21.11 {
            widget.extractRenderState((GuiGraphicsExtractor) g, cursorX, cursorY, 0f);
            //?} else {
            /*widget.render((GuiGraphics) g, cursorX, cursorY, 0f);
             *///?}
        }
    }

    private static boolean clickFlatButton() {
        if (EditorMode.previewing() || !EditorMode.cursorFree()) return false;
        double cursorX = MouseHandlerUtil.getPhysicalX(-1) / pxPerGuiX;
        double cursorY = MouseHandlerUtil.getPhysicalY(-1) / pxPerGuiY;
        for (int i = 0; i < widgets.length; i++) {
            if (widgets[i].isMouseOver(cursorX, cursorY)) {
                EditorMode.BUTTONS[i].action().run();
                return true;
            }
        }
        return false;
    }

    protected EditorScreen() {
        super(Component.literal("Spatial GUI Editor"));
        EditorMode.begin();
        EditorHistory.reset();
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        EditorGizmo.setPressed(false);
        EditorHistory.checkpoint();
        EditorMode.end();
        SpatialGUIConfig.HANDLER.save();
    }

    @Override
    //? if >1.21.11 {
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
    //?} else {
    /*public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
    *///?}
        if (EditorMode.previewing()) {
            int x = (width - 176) / 2;
            int y = (height - 166) / 2;
            //? if >=1.21.6 {
            g.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_TEXTURE, x, y, 0f, 0f, 176, 166, 256, 256);
            //?} else if >=1.21.2 {
            /*g.blit(RenderType::guiTextured, INVENTORY_TEXTURE, x, y, 0f, 0f, 176, 166, 256, 256);
             *///?} else {
            /*g.blit(INVENTORY_TEXTURE, x, y, 0, 0, 176, 166);
             *///?}
            return;
        }

        int gridSize = 18;
        int gray = 0x4A4A4A4A;
        int light_blue = 0xFF33AAFF;
        int red = 0xFFFF2B2B;

        int invWidth = 180;
        int invHeight = 162;
        int invX = (width - invWidth) / 2;
        int invY = (height - invHeight) / 2;

        int startX = invX % gridSize;
        int startY = invY % gridSize;

        // grid
        for (int x = startX; x <= width; x += gridSize) {
            g.verticalLine(x, 0, height, gray);
        }
        for (int y = startY; y <= height; y += gridSize) {
            g.horizontalLine(0, width, y, gray);
        }

        // inventory border
        for (int i = 0; i < 4; i++) {
            g.outline(invX + i, invY + i, invWidth - i * 2, invHeight - i * 2, light_blue);
        }

        // outer border
        for (int i = 0; i < 2; i++) {
            g.outline(i, i, width - i * 2, height - i * 2, red);
        }
    }

    @Override
    public void tick() {
        EditorHistory.tick();
    }

    //? if >1.20.1 {
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    //?} else {
    /*@Override
    public boolean mouseScrolled(double x, double y, double delta) {
    *///?}
        var config = ConfigManager.config();
        //? if >1.20.1 {
        double distanceDelta = scrollY * 0.03;
        //?} else {
        /*double distanceDelta = delta * 0.03;
        *///?}
        if (SpatialGUIClient.getEffectiveFirstPersonMode()) {
            config.firstPersonScreenDistance = Mth.clamp(config.firstPersonScreenDistance + distanceDelta, 0.1, 10.0);
        } else {
            config.screenDistance = Mth.clamp(config.screenDistance + distanceDelta, 0.1, 10.0);
        }
        return true;
    }

    //? if >1.21.1 {
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (handleKeyPress(event.key(), event.hasControlDown(), event.hasShiftDown())) return true;
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (handleKeyRelease(event.key())) return true;
        return super.keyReleased(event);
    }
    //?} else {
    /*@Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (handleKeyPress(keyCode, hasControlDown(), hasShiftDown())) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (handleKeyRelease(keyCode)) return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
    *///?}

    private static boolean handleKeyPress(int key, boolean ctrl, boolean shift) {
        if (!ctrl && key == InputConstants.KEY_SPACE) {
            EditorMode.setPreviewing(true);
            return true;
        }

        if (!ctrl && key == InputConstants.KEY_V) {
            if (!EditorGizmo.isDragging()) EditorMode.toggleView();
            return true;
        }

        if (!ctrl && key == InputConstants.KEY_TAB && SpatialGUIClient.getEffectiveFirstPersonMode()) {
            if (!EditorGizmo.isDragging()) EditorMode.toggleMouseFree();
            return true;
        }

        if (!ctrl) return false;
        if (key == InputConstants.KEY_Z) {
            if (shift) EditorHistory.redo();
            else EditorHistory.undo();
            return true;
        }
        if (key == InputConstants.KEY_Y) {
            EditorHistory.redo();
            return true;
        }
        return false;
    }

    private static boolean handleKeyRelease(int key) {
        if (key == InputConstants.KEY_SPACE) {
            EditorMode.setPreviewing(false);
            return true;
        }
        return false;
    }

    //? if >=26.1.2 {
    public static void drawOverlay(GuiGraphicsExtractor g) {
    //?} else {
    /*public static void drawOverlay(Object g) {
    *///?}
        var client = Minecraft.getInstance();
        //? if <26.2 {
        if (!(client.screen instanceof EditorScreen)) return;
        //?} else {
        /*if (!(client.gui.screen() instanceof EditorScreen)) return;
        *///?}
        if (EditorMode.previewing()) return;

        int y = 6;
        for (String key : EditorMode.getHints()) {
            String line = Component.translatable(key).getString();
            //? if >=26.1.2 {
            g.text(client.font, line, 6, y, 0xFFFFFFFF, true);
            //?} else if >1.21.11 {
            /*((GuiGraphicsExtractor) g).drawString(client.font, line, 6, y, 0xFFFFFFFF, true);
            *///?} else {
            /*((GuiGraphics) g).drawString(client.font, line, 6, y, 0xFFFFFFFF);
            *///?}
            y += 10;
        }

        drawButtons(g, client);
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
        //?}
        if (button == 0 && clickFlatButton()) return true;
        if (button == 0) EditorGizmo.setPressed(true);

        if (button == 0) EditorGizmo.setPressed(true);
        if (button == 1) EditorGizmo.resetHandle();
        if (button == 2) EditorMode.resetScreenDistance();
        //? if >1.21.1 {
        return super.mouseClicked(event, doubleClick);
        //?} else {
        /*return super.mouseClicked(x, y, button);
        *///?}
    }

    //? if >1.21.1 {
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
    //?} else {
    /*@Override
    public boolean mouseReleased(double x, double y, int button) {
    *///?}
        //? if >1.21.1 {
        int button = event.button();
        //?}
        if (button == 0) EditorGizmo.setPressed(false);
        //? if >1.21.1 {
        return super.mouseReleased(event);
        //?} else {
        /*return super.mouseReleased(x, y, button);
        *///?}
    }
}
