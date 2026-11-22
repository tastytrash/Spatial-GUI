package org.tastytrash.spatialGUI.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.compat.ControlifyCompat;
import org.tastytrash.spatialGUI.compat.ShaderCompat;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.editor.EditorGizmo;
import org.tastytrash.spatialGUI.editor.EditorMode;
import org.tastytrash.spatialGUI.mixin.accessor.MouseHandlerAccessor;
import org.tastytrash.spatialGUI.util.AnimationUtil;
import org.tastytrash.spatialGUI.util.CameraUtil;
import org.tastytrash.spatialGUI.util.GuiScaleUtil;
import org.tastytrash.spatialGUI.util.MouseHandlerUtil;
import org.tastytrash.spatialGUI.util.RenderUtil;

//? if fabric {
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
//? } else if neoforge {
/*import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
*///? } else if forge {
/*import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import org.tastytrash.spatialGUI.editor.EditorScreen;
import org.tastytrash.spatialGUI.editor.ProfilePicker;
*///?}
//? if >26.2 {
/*import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
 *///?} else if >1.21.1 {
import com.mojang.blaze3d.buffers.GpuBufferSlice;
//?}
//? if >1.21.1 {
import com.mojang.blaze3d.ProjectionType;
import net.minecraft.client.gui.render.GuiRenderer;
//?}
//? if >1.21.1 && <26.1.2 {
/*import net.minecraft.client.gui.render.state.GuiRenderState;
 *///?}
//? if <=1.21.1 {
/*import com.mojang.blaze3d.vertex.VertexSorting;
import org.joml.Matrix4f;
*///?}

/** Manages the hooked screen's hooking and removal, animation state, rotation anchoring, and mouse mapping. */
public class SpatialGUIRenderer {
    private static long frameCounter;

    public static boolean isExtractingScreen = false;
    /** When true, the window reports its vanilla gui scale/size instead of the override. */
    public static boolean skipWindowOverride = false;
    public static GuiGraphicsExtractor cursorTarget = null;
    public static boolean renderingLevel = false;

    private final ScreenTexture screenTexture;
    private final ScreenExtractor screenExtractor;
    private final WorldScreenRenderer worldScreen;

    private Screen hookedScreen;
    /** Frame the last screen was removed on, to detect a screen swap within a frame. */
    private long removedFrame = -1;
    public boolean poseStarted = false;
    private boolean prevPoseStarted;
    /** Whether {@link #shouldCapture()} was true on the previous call, to detect the first captured frame. */
    private boolean wasCapturing;

    private Vec3 cameraStartPos;
    private float cameraStartYRot;
    private float cameraStartXRot;

    private boolean rotationAnchored;
    private long anchorReleasedFrame = -1;
    private float anchoredYaw;
    private float anchoredPitch;
    /** Player rotation saved before snapping to the camera angle on close (first-person only), restored if swapping. */
    private float[] preSnapState;

    private float fovProgress = 0f;
    private long lastFovUpdateNano = 0L;
    private long lastBobNano = 0L;

    public SpatialGUIRenderer() {
        this.screenTexture = new ScreenTexture();
        this.screenExtractor = new ScreenExtractor();
        this.worldScreen = new WorldScreenRenderer(screenTexture);

        //? if fabric {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen == hookedScreen) registerScreenEvents(screen);
        });
        //?}

        //? if neoforge {
        /*NeoForge.EVENT_BUS.addListener(this::onScreenRenderPre);
        NeoForge.EVENT_BUS.addListener(this::onScreenClosing);
        *///? } else if forge {
        /*MinecraftForge.EVENT_BUS.addListener(this::onScreenRenderPre);
        MinecraftForge.EVENT_BUS.addListener(this::onScreenClosing);
        MinecraftForge.EVENT_BUS.addListener(this::onGuiRenderPost);
        *///? }
    }

    public void hookScreen(Screen screen) {
        if (hookedScreen == screen) return;
        if (!SpatialGUIClient.isEnabled()) return;
        if (screen != currentScreen()) return;

        ConfigManager.clearCache();

        boolean swapping = hookedScreen != null || removedFrame == frameCounter;
        boolean keepAnchor = swapping && (rotationAnchored || anchorReleasedFrame == frameCounter);

        if (hookedScreen != null) onScreenRemoved(false);

        if (!SpatialGUIClient.isEnabled()) {
            return;
        }

        var client = Minecraft.getInstance();
        if (screen != currentScreen()) return;

        if (worldScreen.isClosing()) {
            if (swapping) {
                worldScreen.endClosing();
            }
            else finishClosing();
        }
        var snapPlayer = client.player;
        if (swapping && preSnapState != null && snapPlayer != null) {
            restoreRotation(snapPlayer, preSnapState);
        }
        preSnapState = null;

        skipWindowOverride = false;
        hookedScreen = screen;
        if (swapping) {
            poseStarted = prevPoseStarted;
        } else {
            worldScreen.setScreenOpenTimeMs(System.currentTimeMillis());
        }

        var player = client.player;
        if (player != null) {
            if (!keepAnchor) {
                anchoredYaw = player.getYRot();
                anchoredPitch = player.getXRot();
            }
            rotationAnchored = true;
        }
        if (player != null && Minecraft.getInstance().level != null) {
            CameraUtil.checkBlockCollision(player);
        }

        boolean isFirstPerson = SpatialGUIClient.shouldUseFirstPersonMode(hookedScreen);
        SpatialGUIClient.setEffectiveFirstPersonMode(isFirstPerson);
        MouseHandlerUtil.resetMouseState();
        if (!isFirstPerson) {
            screenTexture.prepareTarget();
        }
    }

    private void registerScreenEvents(Screen screen) {
        //? if fabric && >=26.4 {
        /*ScreenEvents.afterExtract(screen).register((screenArg, extractor) -> prepareTarget());
        *///?} else if fabric && >=26.1.2 {
        ScreenEvents.afterExtract(screen).register((screenArg, extractor, mouseX, mouseY, tickDelta) -> prepareTarget());
        //?} else if fabric {
        /*ScreenEvents.afterRender(screen).register((screenArg, graphics, mouseX, mouseY, tickDelta) -> prepareTarget());
         *///?}

        //? if fabric && 1.20.1 {
        /*var afterRenderEvent = ScreenEvents.afterRender(screen);
        var lateFlushPhase = new net.minecraft.resources.Identifier("spatial_gui", "late_flush");
        afterRenderEvent.addPhaseOrdering(net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE, lateFlushPhase);
        afterRenderEvent.register(lateFlushPhase, (screenArg, graphics, mouseX, mouseY, tickDelta) -> {
            graphics.flush();
            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        });
        *///?}

        //? if fabric {
        ScreenEvents.remove(screen).register(removedScreen -> {
            if (hookedScreen == removedScreen) {
                onScreenRemoved();
            }
        });
        //?}
    }

    //? if neoforge || forge {
    /*private void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (isExtractingScreen) return;
        if (event.getScreen() == hookedScreen) {
            event.setCanceled(true);
            SpatialGUIRenderer.skipWindowOverride = false;
            screenExtractor.extractIsolatedScreen(hookedScreen, event.getPartialTick(), worldScreen.getQuadBasis(), worldScreen.getCylinderBasis(), screenTexture);
            SpatialGUIRenderer.skipWindowOverride = true;
        }
    }
    private void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() == hookedScreen) {
            onScreenRemoved();
        }
    }

    //? if forge {
    /^private void onGuiRenderPost(net.minecraftforge.client.event.RenderGuiEvent.Post event) {
        skipWindowOverride = false;
        EditorScreen.drawOverlay(event.getGuiGraphics());
        ProfilePicker.drawOverlay(event.getGuiGraphics());
    }
    ^///? }
    *///? }

    private void onScreenRemoved() {
        onScreenRemoved(true);
    }

    /** @param animate whether to start the close animation */
    private void onScreenRemoved(boolean animate) {
        if (hookedScreen == null) return;
        skipWindowOverride = false;
        EditorMode.resetMouseFree();
        var player = Minecraft.getInstance().player;
        boolean wasEffectiveFirstPerson = SpatialGUIClient.getEffectiveFirstPersonMode();
        MouseHandlerUtil.releaseMouseFromFirstPerson();
        boolean startClosing = animate
                && ConfigManager.config().enableCloseAnimation
                && Minecraft.getInstance().level != null
                && worldScreen.beginClosing();
        if (!startClosing) {
            worldScreen.resetRecipeBookState();
            worldScreen.resetCameraState();
            releaseRotationAnchor();
        }
        hookedScreen = null;
        removedFrame = frameCounter;
        prevPoseStarted = poseStarted;
        poseStarted = false;
        SpatialGUIClient.setSwitchedToFirstPersonDueToBlock(false);

        preSnapState = null;
        if (wasEffectiveFirstPerson && ConfigManager.config().keepFirstPersonCameraAngle && player != null) {
            preSnapState = captureRotation(player);
            applyCameraSnap(player, cameraYaw(), cameraPitch());
        }
    }

    private void finishClosing() {
        if (!worldScreen.isClosing()) return;
        worldScreen.endClosing();
        worldScreen.resetRecipeBookState();
        worldScreen.resetCameraState();
        screenTexture.clearTarget();
        releaseRotationAnchor();
    }

    /** Runs each frame. */
    public void updateFrame() {
        frameCounter++;
        var mc = Minecraft.getInstance();
        if (hookedScreen != null && currentScreen() != hookedScreen) onScreenRemoved();
        if (worldScreen.isClosing() && (mc.level == null || worldScreen.isCloseFinished())) {
            finishClosing();
        }
        MouseHandlerUtil.syncCursorGrab();
        ControlifyCompat.handleFreeLook(isCrosshairModeActive());
        updateFovProgress();
        updatePausedHandBob();
        EditorGizmo.update();
    }

    public static long frameCounter() {
        return frameCounter;
    }

    private static float[] captureRotation(LocalPlayer player) {
        return new float[]{player.getYRot(), player.getXRot(), player.yRotO, player.xRotO, player.yBob, player.xBob, player.yBobO, player.xBobO};
    }

    private static void restoreRotation(LocalPlayer player, float[] s) {
        player.setYRot(s[0]);
        player.setXRot(s[1]);
        player.yRotO = s[2];
        player.xRotO = s[3];
        player.yBob = s[4];
        player.xBob = s[5];
        player.yBobO = s[6];
        player.xBobO = s[7];
    }

    private static void applyCameraSnap(LocalPlayer player, float cameraYaw, float cameraPitch) {
        player.setYRot(cameraYaw);
        player.setXRot(cameraPitch);
        player.yRotO = cameraYaw;
        player.xRotO = cameraPitch;
    }

    private void releaseRotationAnchor() {
        rotationAnchored = false;
        anchorReleasedFrame = frameCounter;
    }

    /** @return the anchored rotation, or the player's current rotation if not anchored */
    public float anchoredYaw() {
        if (rotationAnchored) return anchoredYaw;
        var player = Minecraft.getInstance().player;
        return player != null ? player.getYRot() : 0f;
    }

    public float anchoredPitch() {
        if (rotationAnchored) return anchoredPitch;
        var player = Minecraft.getInstance().player;
        return player != null ? player.getXRot() : 0f;
    }

    public boolean isClosing() {
        return worldScreen.isClosing();
    }

    public boolean isClosingFirstPerson() {
        return worldScreen.isClosing() && worldScreen.isClosingFirstPerson();
    }

    public boolean isClosingThirdToFirstPerson() {
        return worldScreen.isClosing()
                && !worldScreen.isClosingFirstPerson()
                && !ConfigManager.config().autoDetectCameraMode
                && !SpatialGUIClient.getWasThirdPersonCamera();
    }

    public boolean isClosingWithCameraTransition() {
        return worldScreen.isClosing()
                && WorldScreenRenderer.shouldTransitionCameraOnClose(worldScreen.isClosingFirstPerson());
    }

    public long getCloseElapsedMs() {
        return worldScreen.getCloseElapsedMs();
    }

    public boolean isCameraClosingFinished() {
        float skipFraction = ConfigManager.config().transitionSkipPercentage / 100.0f;
        float cameraMs = Math.max(1, ConfigManager.config().transitionDurationMs) * (1.0f - skipFraction);
        return getCloseElapsedMs() >= cameraMs;
    }

    public float getCloseBlend() {
        if (isCameraClosingFinished()) return 1.0f;
        float duration = Math.max(1, ConfigManager.config().transitionDurationMs);
        float skipFraction = ConfigManager.config().transitionSkipPercentage / 100.0f;
        float openProgress = Math.max(skipFraction, 1.0f - getCloseElapsedMs() / duration);
        return 1.0f - AnimationUtil.easeInOutSine(openProgress);
    }

    /** @return true when the player should draw after the screen. */
    public boolean shouldDeferPlayer() {
        return SpatialGUIClient.isEnabled() && ConfigManager.config().renderPlayerInFront && !ShaderCompat.shaderPackActive()
                //? if <=1.21.1 {
                /*&& !Minecraft.useShaderTransparency()
                *///?}
                && (shouldCapture() ? !SpatialGUIClient.getEffectiveFirstPersonMode() : isClosing() && !isClosingFirstPerson() && !isCameraClosingFinished());
    }

    /** @return true while the hooked screen is the open screen and renders in the world. */
    public boolean shouldCapture() {
        boolean hooked = SpatialGUIClient.shouldHookScreen(hookedScreen) && hookedScreen == currentScreen();
        if (!hooked) {
            wasCapturing = false;
            cameraStartPos = null;
            return false;
        }

        if (!wasCapturing) {
            cameraStartPos = cameraPos();
            cameraStartYRot = cameraYaw();
            cameraStartXRot = cameraPitch();
            wasCapturing = true;
        }

        return true;
    }

    public Vec3 getCameraStartPos() {
        return cameraStartPos;
    }

    public float getCameraStartYRot() {
        return cameraStartYRot;
    }

    public float getCameraStartXRot() {
        return cameraStartXRot;
    }

    public static Screen currentScreen() {
        //? if >=26.2 {
        /*return Minecraft.getInstance().gui.screen();
         *///?} else {
        return Minecraft.getInstance().screen;
        //?}
    }

    private static Vec3 cameraPos() {
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        //? if >=1.21.11 {
        return camera.position();
        //?} else {
        /*return camera.getPosition();
         *///?}
    }

    private static float cameraYaw() {
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        //? if >=1.21.11 {
        return camera.yRot();
        //?} else {
        /*return camera.getYRot();
         *///?}
    }

    private static float cameraPitch() {
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        //? if >=1.21.11 {
        return camera.xRot();
        //?} else {
        /*return camera.getXRot();
         *///?}
    }

    /** Maps the real mouse to the screen each frame and writes it to the mouse handler. */
    public void updateMousePosition() {
        if (!shouldCapture() || MouseHandlerUtil.usesRawMouse(hookedScreen)) return;

        Minecraft client = Minecraft.getInstance();
        MouseHandlerAccessor mouseHandler = (MouseHandlerAccessor) client.mouseHandler;
        boolean isFirstPerson = SpatialGUIClient.getEffectiveFirstPersonMode();

        Vector2d mappedMouse = updateMousePosition(MouseHandlerUtil.getSourceX(), MouseHandlerUtil.getSourceY());

        if (mappedMouse == null) {
            return;
        }

        if (!isFirstPerson) {
            mouseHandler.setRawXpos(mappedMouse.x);
            mouseHandler.setRawYpos(mappedMouse.y);
        }
    }

    /** @return the mouse position in window pixels after mapping onto the screen, or null if the mapping fails */
    public Vector2d updateMousePosition(double sourceX, double sourceY) {
        Minecraft client = Minecraft.getInstance();
        RenderUtil.QuadBasis quadBasis = worldScreen.getQuadBasis();
        if (quadBasis == null) {
            return null;
        }

        if (ControlifyCompat.isVirtualMouseActive() && !isCrosshairModeActive()) {
            MouseHandlerUtil.setFlatPosition(sourceX, sourceY);
            return new Vector2d(sourceX, sourceY);
        }

        double guiScale = ConfigManager.config().getEffectiveGuiScale();
        Vector2d mappedMouse = MouseHandlerUtil.mapMousePosition(
                sourceX, sourceY, quadBasis, worldScreen.getCylinderBasis(), guiScale
        );

        if (mappedMouse == null) {
            return null;
        }

        double mappedX = mappedMouse.x / guiScale;
        double mappedY = mappedMouse.y / guiScale;
        mappedX *= (double) client.getWindow().getScreenWidth() / client.getWindow().getGuiScaledWidth();
        mappedY *= (double) client.getWindow().getScreenHeight() / client.getWindow().getGuiScaledHeight();

        return new Vector2d(mappedX, mappedY);
    }

    public boolean isMouseOverScreen() {
        if (hookedScreen == null) return false;
        return RenderUtil.isMouseOver(
                MouseHandlerUtil.getSourceX(), MouseHandlerUtil.getSourceY(),
                worldScreen.getQuadBasis(), worldScreen.getCylinderBasis());
    }

    public static boolean isCrosshairModeActive() {
        return ConfigManager.config() != null
                && ConfigManager.config().useCrosshairForFirstPerson
                && SpatialGUIClient.getEffectiveFirstPersonMode()
                && SpatialGUIClient.renderer() != null
                && SpatialGUIClient.renderer().getHookedScreen() != null;
    }

    public void updateFovProgress() {
        var mc = Minecraft.getInstance();
        if (mc.level == null) {
            fovProgress = 0f;
            lastFovUpdateNano = 0L;
            return;
        }

        Screen current = currentScreen();

        boolean active = current != null && current == hookedScreen && SpatialGUIClient.shouldHookScreen(current);
        float target = (active && ConfigManager.config().overrideFov) ? 1f : 0f;
        float speed = ConfigManager.config().fovLerpSpeed;

        if (speed <= 0f) {
            fovProgress = target;
            lastFovUpdateNano = 0L;
            return;
        }

        long now = System.nanoTime();
        float dt = lastFovUpdateNano == 0L ? 0f : Math.min((now - lastFovUpdateNano) / 1000000000f, 0.1f);
        lastFovUpdateNano = now;

        float alpha = 1f - (float) Math.exp(-dt * speed);
        fovProgress += (target - fovProgress) * alpha;

        if (Math.abs(target - fovProgress) < 0.001f) fovProgress = target;
    }

    public float getFovOverrideBlend() {
        return fovProgress;
    }

    public boolean bobSwayActive() {
        return SpatialGUIClient.isEnabled()
                && SpatialGUIClient.getEffectiveFirstPersonMode()
                && (hookedScreen != null || (isClosingFirstPerson() && !SpatialGUIClient.getWasThirdPersonCamera()))
                && !ConfigManager.config().hideHandsInFirstPerson;
    }

    /** Keeps the hand bob running while the game is paused. */
    private void updatePausedHandBob() {
        long now = System.nanoTime();
        float dt = lastBobNano == 0L ? 0f : Math.min((now - lastBobNano) / 1_000_000_000f, 0.1f);
        lastBobNano = now;

        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null || !mc.isPaused() || !bobSwayActive()) return;

        float a = 1f - (float) Math.pow(0.5, dt * 20.0);
        float pitch = player.xBob + (CameraUtil.pitch() - player.xBob) * a;
        float yaw = player.yBob + Mth.wrapDegrees(CameraUtil.yaw() - player.yBob) * a;

        player.xBobO = pitch;
        player.xBob = pitch;
        player.yBobO = yaw;
        player.yBob = yaw;
    }

    //? if >1.21.1 {
    public void captureCameraState(GpuBufferSlice buffer, ProjectionType type, PoseStack poseStack) {
        worldScreen.captureCameraState(buffer, type, poseStack);
    }
    //?} else {
    /*public void captureCameraState(Matrix4f projectionMatrix, VertexSorting vertexSorting, PoseStack poseStack) {
        worldScreen.captureCameraState(projectionMatrix, vertexSorting, poseStack);
    }
    *///?}

    public void renderInWorld(PoseStack matrices) {
        worldScreen.renderInWorld(matrices);
    }

    public void renderInWorldPost() {
        worldScreen.renderInWorldPost();
    }

    public void onFrameStart() {
        GuiScaleUtil.pendingScreen = null;
        worldScreen.onFrameStart();
    }

    //? if >1.21.1 {
    public GuiRenderer getScreenGuiRenderer() {
        return screenExtractor.getScreenGuiRenderer();
    }
    //? if <26.1.2 {
    /*public GuiRenderState getScreenRenderState() {
        return screenExtractor.getScreenRenderState();
    }
    *///?}

    public GpuBufferSlice getCapturedProjectionBuffer() {
        return worldScreen.getCapturedProjectionBuffer();
    }

    public ProjectionType getCapturedProjectionType() {
        return worldScreen.getCapturedProjectionType();
    }
    //?}

    //? if >1.21.1 {
    public GuiGraphicsExtractor createIsolatedGraphics() {
        return screenExtractor.createIsolatedGraphics();
    }
    //?}

    public void extractIsolatedScreen(Screen screen, float partialTick) {
        screenExtractor.extractIsolatedScreen(screen, partialTick, worldScreen.getQuadBasis(), worldScreen.getCylinderBasis(), screenTexture);
    }

    public void extractIsolatedScreen(Screen screen, float partialTick, Operation<Void> operation) {
        screenExtractor.extractIsolatedScreen(screen, partialTick, worldScreen.getQuadBasis(), worldScreen.getCylinderBasis(), screenTexture, operation);
    }

    public void clearTarget() {
        screenTexture.clearTarget();
    }

    public void prepareTarget() {
        screenTexture.prepareTarget();
    }

    public Screen getHookedScreen() {
        return hookedScreen;
    }

    public ScreenTexture getScreenTexture() {
        return screenTexture;
    }

    public WorldScreenRenderer getWorldScreenRenderer() {
        return worldScreen;
    }
}