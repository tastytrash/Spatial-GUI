package org.tastytrash.spatialGUI.render;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.util.MouseHandlerUtil;
import org.tastytrash.spatialGUI.util.CameraUtil;

public class SpatialGUIRenderer {
    public static boolean isExtractingIsolatedScreen = false;
    public static boolean suppressWindowOverride = false;

    private final TextureTargetManager targetManager;
    private final ScreenExtractor screenExtractor;
    private final InventoryRenderer inventoryRenderer;
    private final Vector2d[] screenCorners;

    private Screen hookedScreen;
    private static boolean isInventoryScreen;
    private Vec3 cameraStartPos;
    private float cameraStartYRot;
    private static boolean wasTrue;
    public boolean headLockInitialized = false;

    public SpatialGUIRenderer() {
        this.screenCorners = new Vector2d[]{
                new Vector2d(),
                new Vector2d(),
                new Vector2d(),
                new Vector2d()
        };
        this.targetManager = new TextureTargetManager();
        this.screenExtractor = new ScreenExtractor();
        this.inventoryRenderer = new InventoryRenderer(targetManager, screenCorners);
    }

    public void hookScreen(Screen screen) {
        if (hookedScreen == screen) {
            return;
        }

        if (!SpatialGUI.config.enabled) {
            return;
        }

        hookedScreen = screen;
        isInventoryScreen = screen instanceof InventoryScreen || screen.getClass().getName().contains("InventoryScreen");
        inventoryRenderer.setScreenOpenTime(System.currentTimeMillis());

        var client = Minecraft.getInstance();

        var player = client.player;
        if (player != null && Minecraft.getInstance().level != null) {
            CameraUtil.checkCameraCollision(player);
        }

        boolean isFirstPerson = (hookedScreen instanceof InventoryScreen ? SpatialGUI.config.firstPersonModeInventory : SpatialGUI.config.firstPersonModeContainers)
                || SpatialGUIClient.getSwitchedToFirstPersonDueToBlock();
        SpatialGUIClient.setEffectiveFirstPersonMode(isFirstPerson);

        ScreenEvents.afterExtract(screen).register((_, _, _, _, _) -> prepareTarget());

        ScreenEvents.remove(screen).register(
                removedScreen -> {
                    if (hookedScreen == removedScreen) {
                        MouseHandlerUtil.releaseMouseFromFirstPerson();
                        hookedScreen = null;
                        isInventoryScreen = false;
                        cameraStartPos = null;
                        headLockInitialized = false;
                        SpatialGUIClient.setSwitchedToFirstPersonDueToBlock(false);

                        if (SpatialGUIClient.getEffectiveFirstPersonMode() && SpatialGUI.config.keepFirstPersonCameraAngle) {
                            float cameraYaw = Minecraft.getInstance().gameRenderer.mainCamera().yRot();
                            float cameraPitch = Minecraft.getInstance().gameRenderer.mainCamera().xRot();
                            if (player != null) {
                                player.setYRot(cameraYaw);
                                player.setXRot(cameraPitch);
                                player.yRotO = cameraYaw;
                                player.xRotO = cameraPitch;

                                player.yBob = cameraYaw;
                                player.xBob = cameraPitch;
                                player.yBobO = cameraYaw;
                                player.xBobO = cameraPitch;
                            }
                        }
                    }
                }
        );
    }

    public void clearTarget() {
        targetManager.clearTarget();
    }

    public void prepareTarget() {
        targetManager.prepareTarget();
    }

    public boolean shouldCapture() {
        Minecraft client = Minecraft.getInstance();
        boolean bool = hookedScreen instanceof AbstractContainerScreen<?> && hookedScreen == client.gui.screen() && SpatialGUI.config.enabled;
        if (!bool) {
            wasTrue = false;
            return false;
        }

        if (!wasTrue) {
            cameraStartPos = Minecraft.getInstance().gameRenderer.mainCamera().position();
            cameraStartYRot = Minecraft.getInstance().gameRenderer.mainCamera().yRot();
            wasTrue = true;
        }

        return true;
    }

    public Vec3 getCameraStartPos() {
        return cameraStartPos;
    }

    public float getCameraStartYRot() {
        return cameraStartYRot;
    }

    public static boolean isInventoryScreen() {
        return isInventoryScreen;
    }

    public Screen getHookedScreen() {
        return hookedScreen;
    }

    public Vector2d[] getScreenCorners() {
        return screenCorners;
    }

    public TextureTargetManager getTargetManager() {
        return targetManager;
    }

    public InventoryRenderer getInventoryRenderer() {
        return inventoryRenderer;
    }

    public static boolean isCrosshairModeActive() {
        return SpatialGUI.config.useCrosshairForFirstPerson
                && SpatialGUIClient.getEffectiveFirstPersonMode()
                && SpatialGUIClient.renderer() != null
                && SpatialGUIClient.renderer().getHookedScreen() != null;
    }

    public void renderInWorld(com.mojang.blaze3d.vertex.PoseStack matrices) {
        inventoryRenderer.renderInWorld(matrices);
    }

    public net.minecraft.client.gui.render.GuiRenderer getScreenGuiRenderer() {
        return screenExtractor.getScreenGuiRenderer();
    }

    public void extractIsolatedScreen(Screen screen, float partialTick) {
        screenExtractor.extractIsolatedScreen(screen, partialTick, screenCorners, targetManager);
    }

}