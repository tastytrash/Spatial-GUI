package org.tastytrash.spatialGUI.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.render.pip.*;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.joml.Vector2d;
import org.tastytrash.spatialGUI.util.RenderUtil;
import org.tastytrash.spatialGUI.util.RenderUtil.QuadBasis;

//? if neoforge {
import net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration;
import net.minecraft.client.renderer.state.gui.pip.*;
//? }

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
                    //? if fabric {
//                    List.of(
//                            new GuiEntityRenderer(mc.getEntityRenderDispatcher()),
//                            new GuiSkinRenderer(),
//                            new GuiBookModelRenderer(),
//                            new GuiBannerResultRenderer(mc.getAtlasManager()),
//                            new GuiProfilerChartRenderer()
//                    )
                    //? } else if neoforge {
                    List.of(
                            new PictureInPictureRendererRegistration<>(GuiEntityRenderState.class, () -> new GuiEntityRenderer(mc.getEntityRenderDispatcher())),
                            new PictureInPictureRendererRegistration<>(GuiSkinRenderState.class, GuiSkinRenderer::new),
                            new PictureInPictureRendererRegistration<>(GuiBookModelRenderState.class, GuiBookModelRenderer::new),
                            new PictureInPictureRendererRegistration<>(GuiBannerResultRenderState.class, () -> new GuiBannerResultRenderer(mc.getAtlasManager())),
                            new PictureInPictureRendererRegistration<>(GuiProfilerChartRenderState.class, GuiProfilerChartRenderer::new)
                    )
                    //? }
            );
        }
    }

    public GuiRenderer getScreenGuiRenderer() {
        ensureScreenGuiRenderer();
        return screenGuiRenderer;
    }

    public void extractIsolatedScreen(Screen screen, float partialTick, QuadBasis quadBasis, TextureTargetManager targetManager) {
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

        Vector2d mapped = null;
        if (quadBasis != null) {
            mapped = RenderUtil.getInventoryMousePositionRay(srcX, srcY, quadBasis, targetManager.getInventoryTarget());
        }
        int mouseX, mouseY;
        if (mapped != null) {
            double guiScale = mc.getWindow().getGuiScale();
            mouseX = (int) (mapped.x / guiScale);
            mouseY = (int) (mapped.y / guiScale);
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        } else {
            mouseX = (int) mc.mouseHandler.getScaledXPos(mc.getWindow());
            mouseY = (int) mc.mouseHandler.getScaledYPos(mc.getWindow());
            if (lastMouseX == -1) {
                lastMouseX = mouseX;
                lastMouseY = mouseY;
            }
        }

        SpatialGUIRenderer.isExtractingIsolatedScreen = true;
        GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(mc, screenRenderState, mouseX, mouseY);
        screen.extractRenderStateWithTooltipAndSubtitles(graphics, mouseX, mouseY, partialTick);
        SpatialGUIRenderer.isExtractingIsolatedScreen = false;
    }
}
