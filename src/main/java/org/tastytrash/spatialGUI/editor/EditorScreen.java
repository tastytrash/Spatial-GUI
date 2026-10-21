package org.tastytrash.spatialGUI.editor;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
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
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.config.SpatialGUIConfig;

public class EditorScreen extends Screen {
    //? if >=1.21.11 {
    private static final Identifier INVENTORY_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/inventory.png");
    //?} else if >1.20.1 {
    /*private static final ResourceLocation INVENTORY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");
     *///?} else {
    /*private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation("textures/gui/container/inventory.png");
     *///?}

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
        for (int i = 0; i < 2; i++) {
            g.outline(invX + i, invY + i, invWidth - i * 2, invHeight - i * 2, light_blue);
        }

        // outer border
        for (int i = 0; i < 4; i++) {
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
        boolean fp = org.tastytrash.spatialGUI.client.SpatialGUIClient.getEffectiveFirstPersonMode();
        //? if >1.20.1 {
        double distanceDelta = scrollY * 0.03;
        //?} else {
        /*double distanceDelta = delta * 0.03;
        *///?}
        if (fp) {
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
    public static void extractHints(GuiGraphicsExtractor g) {
    //?} else {
    /*public static void extractHints(Object g) {
    *///?}
        var client = Minecraft.getInstance();
        //? if <26.2 {
        if (!(client.screen instanceof EditorScreen)) return;
        //?} else {
        /*if (!(client.gui.screen() instanceof EditorScreen)) return;
        *///?}
        if (EditorMode.previewing()) return;

        int y = 6;
        for (String line : EditorMode.HINTS) {
            //? if >=26.1.2 {
            g.text(client.font, line, 6, y, 0xFFFFFFFF, true);
            //?} else if >1.21.11 {
            /*((GuiGraphicsExtractor) g).drawString(client.font, line, 6, y, 0xFFFFFFFF, true);
            *///?} else {
            /*((GuiGraphics) g).drawString(client.font, line, 6, y, 0xFFFFFFFF);
            *///?}
            y += 10;
        }
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
        if (button == 0) EditorGizmo.setPressed(true);
        if (button == 1) EditorGizmo.resetHandle();
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
