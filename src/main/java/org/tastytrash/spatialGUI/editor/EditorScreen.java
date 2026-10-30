package org.tastytrash.spatialGUI.editor;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.util.Mth;
//? if >1.21.1 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
//?}
import net.minecraft.client.gui.GuiGraphicsExtractor;

//? if <1.21.11 {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.config.SpatialGUIConfig;
import org.tastytrash.spatialGUI.util.EditorUtil;
import org.tastytrash.spatialGUI.util.GuiScaleUtil;

import java.util.Arrays;

public class EditorScreen extends Screen {
    //? if >=1.21.11 {
    private static final Identifier INVENTORY_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/inventory.png");
    //?} else if >1.20.1 {
    /*private static final ResourceLocation INVENTORY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");
     *///?} else {
    /*private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation("textures/gui/container/inventory.png");
     *///?}

    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_SPACING = 4;
    private static final Button[] widgets = new Button[EditorMode.BUTTONS.length];

    static {
        Arrays.setAll(widgets, i -> EditorUtil.button(BUTTON_WIDTH));
    }

    private static void drawButtons(Object g, Minecraft client) {
        var window = client.getWindow();
        int guiWidth = window.getGuiScaledWidth();
        int guiHeight = window.getGuiScaledHeight();
        boolean usable = EditorMode.cursorFree();

        int leftCount = 0, rightCount = 0;
        for (int i = widgets.length - 1; i >= 0; i--) {
            var button = EditorMode.BUTTONS[i];
            boolean right = button.rightSide();
            int slot = right ? rightCount++ : leftCount++;
            int x = right ? guiWidth - EditorUtil.SCREEN_MARGIN - BUTTON_WIDTH : EditorUtil.SCREEN_MARGIN;
            int y = guiHeight - EditorUtil.SCREEN_MARGIN - EditorUtil.BUTTON_HEIGHT - slot * (EditorUtil.BUTTON_HEIGHT + BUTTON_SPACING);
            EditorUtil.drawButton(widgets[i], button.label().get(), x, y, usable && button.enabled().getAsBoolean(), g);
        }

        EditorDropdown.draw(g, guiWidth);
        EditorHelp.draw(g, guiWidth, guiHeight);
    }

    private static boolean clickFlatButton(int mouseButton) {
        if (EditorMode.previewing() || !EditorMode.cursorFree()) return false;
        double cursorX = EditorUtil.cursorX();
        double cursorY = EditorUtil.cursorY();
        if (EditorHelp.clickHelpButton(cursorX, cursorY, mouseButton)) return true;
        if (EditorDropdown.click(cursorX, cursorY, mouseButton)) return true;
        if (mouseButton != InputConstants.MOUSE_BUTTON_LEFT) return false;
        for (int i = 0; i < widgets.length; i++) {
            if (widgets[i].isMouseOver(cursorX, cursorY)) {
                EditorMode.BUTTONS[i].action().run();
                return true;
            }
        }
        return false;
    }

    protected EditorScreen() {
        super(Component.translatable("spatial-gui.editor.title"));
        EditorMode.begin();
        EditorProfiles.begin();
        EditorHistory.reset();
        EditorDropdown.close();
        EditorHelp.begin();
    }

    @Override
    public boolean isPauseScreen() {
        return SpatialGUIConfig.get().editorPausesGame;
    }

    @Override
    public void removed() {
        EditorDropdown.close();
        EditorGizmo.setPressed(false);
        EditorHistory.checkpoint();
        EditorProfiles.end();
        EditorMode.end();
        SpatialGUIConfig.HANDLER.save();
    }

    @Override
    //? if >1.21.11 {
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
    //?} else {
    /*public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
    *///?}
        if (EditorMode.previewing()) {
            int x = (width - 176) / 2;
            int y = (height - 166) / 2;
            //? if >1.21.1 {
            g.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_TEXTURE, x, y, 0f, 0f, 176, 166, 256, 256);
            //?} else {
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
        if (EditorHelp.isOpen()) return true;
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
        if (EditorHelp.isOpen()) return EditorHelp.keyPressed(event.key());
        if (EditorDropdown.keyPressed(event)) return true;
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
        if (EditorHelp.isOpen()) return EditorHelp.keyPressed(keyCode);
        if (EditorDropdown.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (handleKeyPress(keyCode, hasControlDown(), hasShiftDown())) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (handleKeyRelease(keyCode)) return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
    *///?}

    //? if >1.21.1 {
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (EditorDropdown.charTyped(event)) return true;
        return super.charTyped(event);
    }
    //?} else {
    /*@Override
    public boolean charTyped(char character, int modifiers) {
        if (EditorDropdown.charTyped(character, modifiers)) return true;
        return super.charTyped(character, modifiers);
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

        float scale = GuiScaleUtil.overlayScaleRatio(client.getWindow());
        EditorUtil.pushScale(g, scale);

        int y = EditorUtil.SCREEN_MARGIN;
        for (String key : EditorMode.getHints()) {
            EditorUtil.drawTranslatedText(g, key, EditorUtil.SCREEN_MARGIN, y);
            y += 10;
        }
        drawButtons(g, client);

        EditorUtil.popScale(g);
    }

    //? if >1.21.1 {
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    //?} else {
    /*@Override
    public boolean mouseClicked(double x, double y, int button) {
    *///?}
        int left = InputConstants.MOUSE_BUTTON_LEFT;
        int right = InputConstants.MOUSE_BUTTON_RIGHT;
        int middle = InputConstants.MOUSE_BUTTON_MIDDLE;
        //? if >1.21.1 {
        int button = event.button();
        //?}
        if (EditorHelp.isOpen()) {
            if (button == left && EditorMode.cursorFree()) {
                EditorHelp.click(EditorUtil.cursorX(), EditorUtil.cursorY());
            }
            return true;
        }
        if ((button == left || button == right) && clickFlatButton(button)) return true;
        if (button == left) EditorGizmo.setPressed(true);
        if (button == right) EditorGizmo.resetSelectedHandle();
        if (button == middle) EditorMode.resetScreenDistance();
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
        if (button == InputConstants.MOUSE_BUTTON_LEFT) EditorGizmo.setPressed(false);
        //? if >1.21.1 {
        return super.mouseReleased(event);
        //?} else {
        /*return super.mouseReleased(x, y, button);
        *///?}
    }
}