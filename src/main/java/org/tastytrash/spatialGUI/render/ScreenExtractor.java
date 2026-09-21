package org.tastytrash.spatialGUI.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.render.pip.*;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.joml.Vector2d;
import org.tastytrash.spatialGUI.util.RenderUtil;

import java.util.List;

public class ScreenExtractor {
    private GuiRenderState screenRenderState;
    private GuiRenderer screenGuiRenderer;
    private static int lastMouseX = -1;
    private static int lastMouseY = -1;

    public void ensureScreenGuiRenderer() {
        if (screenGuiRenderer == null) {
            Minecraft mc = Minecraft.getInstance();
            screenRenderState = new GuiRenderState();
            screenGuiRenderer = new GuiRenderer(
                    screenRenderState,
                    mc.gameRenderer.featureRenderDispatcher(),
                    List.of(
                            new GuiEntityRenderer(mc.getEntityRenderDispatcher()),
                            new GuiSkinRenderer(),
                            new GuiBookModelRenderer(),
                            new GuiBannerResultRenderer(mc.getAtlasManager()),
                            new GuiProfilerChartRenderer()
                    )
            );
        }
    }

    public GuiRenderer getScreenGuiRenderer() {
        ensureScreenGuiRenderer();
        return screenGuiRenderer;
    }

    public void extractIsolatedScreen(Screen screen, float partialTick, Vector2d[] screenCorners, TextureTargetManager targetManager) {
        ensureScreenGuiRenderer();
        Minecraft mc = Minecraft.getInstance();

        double srcX, srcY;
        if (SpatialGUIRenderer.isCrosshairModeActive()) {
            srcX = mc.getWindow().getScreenWidth() / 2.0;
            srcY = mc.getWindow().getScreenHeight() / 2.0;
        } else {
            srcX = mc.mouseHandler.xpos();
            srcY = mc.mouseHandler.ypos();
        }

        Vector2d mapped = RenderUtil.getInventoryMousePosition(srcX, srcY, screenCorners, targetManager.getInventoryTarget());
        int mouseX, mouseY;
        if (mapped != null) {
            double guiScale = mc.getWindow().getGuiScale();
            mouseX = (int) (mapped.x / guiScale);
            mouseY = (int) (mapped.y / guiScale);
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        } else {
            mouseX = lastMouseX;
            mouseY = lastMouseY;
        }

        SpatialGUIRenderer.isExtractingIsolatedScreen = true;
        GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(mc, screenRenderState, mouseX, mouseY);
        screen.extractRenderStateWithTooltipAndSubtitles(graphics, mouseX, mouseY, partialTick);
        SpatialGUIRenderer.isExtractingIsolatedScreen = false;
    }
}
