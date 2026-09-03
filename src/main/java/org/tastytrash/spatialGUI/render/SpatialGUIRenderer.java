package org.tastytrash.spatialGUI.render;

//? > 26.2 {
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
//? } else {
//import com.mojang.blaze3d.GpuFormat;
//import com.mojang.blaze3d.PrimitiveTopology;
//import com.mojang.blaze3d.buffers.GpuBufferSlice;
//import com.mojang.blaze3d.pipeline.RenderPipeline;
//import com.mojang.blaze3d.textures.AddressMode;
//import com.mojang.blaze3d.textures.FilterMode;
//import com.mojang.blaze3d.textures.GpuTextureView;
//import com.mojang.blaze3d.vertex.VertexFormat;
//? }
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.util.MathUtil;
import org.tastytrash.spatialGUI.util.RenderUtil;
import org.tastytrash.spatialGUI.util.AnimationUtil;

import java.lang.Math;

public class SpatialGUIRenderer {

    private static TextureTarget inventoryTarget;
    private Screen hookedScreen;
    private Vec3 cameraStartPos;
    private float cameraStartYRot;
    private static boolean wasTrue;
    public boolean headLockInitialized = false;
    public static boolean isRecipeBookOpen = false;
    private static int recipeBookCloseDelay = 0;
    private long screenOpenTime = 0;
    public static boolean hadDebug = false;
    public static boolean hadHideHUD = false;

    private static final Vector2d[] screenCorners = {
            new Vector2d(),
            new Vector2d(),
            new Vector2d(),
            new Vector2d()
    };

    private static final RenderPipeline INVENTORY_PIPELINE =
            RenderPipelines.GUI_TEXTURED;

    private static final StagedVertexBuffer INVENTORY_BUFFER =
            new StagedVertexBuffer(
                    () -> "Spatial GUI Inventory Buffer",
                    RenderType.SMALL_BUFFER_SIZE
            );

    public void hookScreen(Screen screen) {
        if (hookedScreen == screen) {
            return;
        }

        if (!SpatialGUI.config.enabled) {
            return;
        }

        hookedScreen = screen;
        screenOpenTime = System.currentTimeMillis();

        var client = Minecraft.getInstance();
        var hud = client.gui.hud;
        hadHideHUD = hud.isHidden();
        hadDebug = client.debugEntries.isOverlayVisible();

        var player = client.player;
        if (player != null && Minecraft.getInstance().level != null) {
            float thirdPersonDistance = (float) SpatialGUI.config.cameraDistance;
            float thirdPersonSideOffset = (float) SpatialGUI.config.cameraSideOffset;
            float thirdPersonHeightOffset = (float) SpatialGUI.config.cameraHeightOffset;
    float thirdPersonYaw = player .getYRot();
            float thirdPersonYawRadians = (float) Math.toRadians(thirdPersonYaw);

            double thirdPersonCamX = player.getX() + Math.sin(thirdPersonYawRadians) * thirdPersonDistance + Math.cos(thirdPersonYawRadians) * thirdPersonSideOffset;
            double thirdPersonCamY = player.getY() + thirdPersonHeightOffset;
            double thirdPersonCamZ = player.getZ() - Math.cos(thirdPersonYawRadians) * thirdPersonDistance + Math.sin(thirdPersonYawRadians) * thirdPersonSideOffset;

            Vec3 playerEyePos = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
            Vec3 targetCamPos = new Vec3(thirdPersonCamX, thirdPersonCamY, thirdPersonCamZ);

            var blockState = Minecraft.getInstance().level.getBlockState(BlockPos.containing(thirdPersonCamX, thirdPersonCamY, thirdPersonCamZ));
            var collisionShape = blockState.getCollisionShape(Minecraft.getInstance().level, BlockPos.containing(thirdPersonCamX, thirdPersonCamY, thirdPersonCamZ));
            boolean isInsideBlock = !blockState.isAir() && !collisionShape.isEmpty() && collisionShape.bounds().move(BlockPos.containing(thirdPersonCamX, thirdPersonCamY, thirdPersonCamZ)).inflate(0.001).contains(targetCamPos);

            var clipContext = new ClipContext(
                playerEyePos,
                targetCamPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
            );
            var raycastResult = Minecraft.getInstance().level.clip(clipContext);
            boolean pathBlocked = raycastResult.getType() != HitResult.Type.MISS;

            if (isInsideBlock || pathBlocked) {
                SpatialGUIClient.setSwitchedToFirstPersonDueToBlock(true);
            }
        }

        boolean isFirstPerson = SpatialGUI.config.firstPersonMode
                || SpatialGUIClient.getSwitchedToFirstPersonDueToBlock();
        SpatialGUIClient.setEffectiveFirstPersonMode(isFirstPerson);

        ScreenEvents.afterExtract(screen).register((_, _, _, _, _) -> prepareTarget());

        ScreenEvents.remove(screen).register(
                removedScreen -> {
                    if (hookedScreen == removedScreen) {
                        hookedScreen = null;
                        cameraStartPos = null;
                        headLockInitialized = false;
                        SpatialGUIClient.setSwitchedToFirstPersonDueToBlock(false);
                        if (hadHideHUD != client.gui.hud.isHidden()) {
                            client.gui.hud.toggle();
                        }
                        if (hadDebug != client.debugEntries.isOverlayVisible()) {
                            client.debugEntries.toggleDebugOverlay();
                        }
                    }
                }
        );
    }


    public void clearTarget() {
        if (inventoryTarget == null) {
            return;
        }

        var colorTexture = inventoryTarget.getColorTexture();
        var depthTexture = inventoryTarget.getDepthTexture();

        if (colorTexture == null) {
            return;
        }

        var encoder = RenderSystem.getDevice().createCommandEncoder();

        encoder.clearColorTexture(
                colorTexture,
                new Vector4f(0.0F, 0.0F, 0.0F, 0.0F)
        );

        if (depthTexture != null) {
            encoder.clearDepthTexture(
                    depthTexture,
                    1.0
            );
        }
    }

    public void prepareTarget() {
        Minecraft client = Minecraft.getInstance();

        int width = client.getWindow().getWidth();
        int height = client.getWindow().getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        if (inventoryTarget == null) {

            inventoryTarget = new TextureTarget(
                    "Spatial GUI Inventory",
                    width,
                    height,
                    //? if > 26.2 {
                    GpuFormat.RGBA8_UNORM,
                    GpuFormat.D16_UNORM
                    //? } else {
//                    true,
//                    GpuFormat.RGBA8_UNORM
                    //? }
            );
            return;
        }

        if (inventoryTarget.width != width
                || inventoryTarget.height != height) {
            inventoryTarget.resize(width, height);
        }
    }

    public TextureTarget getTarget() {
        prepareTarget();
        return inventoryTarget;
    }

    public boolean shouldCapture() {
        Minecraft client = Minecraft.getInstance();
        boolean bool = hookedScreen instanceof AbstractContainerScreen<?> && hookedScreen == client.gui.screen() && SpatialGUI.config.enabled;
        var hud = client.gui.hud;

        if (!bool) {
            wasTrue = false;
            return false;
        }

        if (!wasTrue) {
            cameraStartPos = Minecraft.getInstance().gameRenderer.mainCamera().position();
            cameraStartYRot = Minecraft.getInstance().gameRenderer.mainCamera().yRot();
            wasTrue = true;
            if (!hud.isHidden()) hud.toggle();
            if (client.debugEntries.isOverlayVisible()) client.debugEntries.toggleDebugOverlay();
        }

        return true;
    }

    public Vec3 getCameraStartPos() {
        return cameraStartPos;
    }

    public float getCameraStartYRot() {
        return cameraStartYRot;
    }

    public void renderInWorld(PoseStack matrices) {
        Minecraft client = Minecraft.getInstance();

        if (inventoryTarget == null || !shouldCapture() || client.player == null) {
            return;
        }

        GpuTextureView texture = inventoryTarget.getColorTextureView();
        RenderPipeline pipeline = INVENTORY_PIPELINE;
        VertexFormat format = pipeline.getVertexFormatBinding(0);

        if (texture == null || format == null) {
            return;
        }

        PrimitiveTopology primitive = pipeline.getPrimitiveTopology();
        StagedVertexBuffer.Draw draw = INVENTORY_BUFFER.appendDraw(
                format,
                primitive,
                primitive == PrimitiveTopology.QUADS
                        ? RenderSystem.getProjectionType().vertexSorting()
                        : null
        );

        matrices.pushPose();

        var player = client.player;
        boolean isFirstPerson = SpatialGUIClient.getEffectiveFirstPersonMode();
        float yaw = player.getYRot();
        float pitch = isFirstPerson ? player.getXRot() : 0;
        float yawRadians = (float) Math.toRadians(yaw);
        float pitchRadians = (float) Math.toRadians(pitch);

        RenderUtil.ScreenTransformConfig config = RenderUtil.getScreenTransformConfig(isFirstPerson);

        double lookX = -Math.sin(yawRadians) * Math.cos(pitchRadians);
        double lookY = -Math.sin(pitchRadians);
        double lookZ = Math.cos(yawRadians) * Math.cos(pitchRadians);

        RenderUtil.applyScreenTransform(matrices, isFirstPerson, yawRadians, pitchRadians, config, lookX, lookY, lookZ);

        float scale = config.scale();

        recipeBookCloseDelay = isRecipeBookOpen ? -2 : Math.min(0, recipeBookCloseDelay + 1);
        scale = (isRecipeBookOpen || recipeBookCloseDelay < 0) ? scale / (float) SpatialGUI.config.recipeBookShrinkFactor : scale;

        if (SpatialGUI.config.enableScaleAnimation) {
            long elapsed = System.currentTimeMillis() - screenOpenTime;
            float animationProgress = Math.min(1.0F, (float) elapsed / (float) SpatialGUI.config.openAnimationDurationMs);
            float easedProgress = AnimationUtil.applyEasing(SpatialGUI.config.animationEasing, animationProgress);
            float startScalePercent = SpatialGUI.config.animationStartScalePercent / 100.0f;
            float startScale = scale * startScalePercent;
            scale = MathUtil.lerp(startScale, scale, easedProgress);
        }

        matrices.scale(scale, scale, scale);

        Matrix4f pose = matrices.last().pose();
        VertexConsumer buffer = INVENTORY_BUFFER.getVertexBuilder(draw);

        float aspect = (float) inventoryTarget.width / (float) inventoryTarget.height;
        float halfWidth = aspect * 0.5F;
        float halfHeight = 0.5F;

        RenderUtil.addQuadVertex(buffer, pose, -halfWidth, -halfHeight, 0.0F, 0.0F, 0.0F);
        RenderUtil.addQuadVertex(buffer, pose, halfWidth, -halfHeight, 0.0F, 1.0F, 0.0F);
        RenderUtil.addQuadVertex(buffer, pose, halfWidth, halfHeight, 0.0F, 1.0F, 1.0F);
        RenderUtil.addQuadVertex(buffer, pose, -halfWidth, halfHeight, 0.0F, 0.0F, 1.0F);

        Vector3f[] corners = {
                new Vector3f(-halfWidth, -halfHeight, 0.0F),
                new Vector3f( halfWidth, -halfHeight, 0.0F),
                new Vector3f( halfWidth,  halfHeight, 0.0F),
                new Vector3f(-halfWidth,  halfHeight, 0.0F)
        };

        int width = client.getWindow().getWidth();
        int height = client.getWindow().getHeight();

        Vec3 cameraPos = client.gameRenderer.mainCamera().position();

        PoseStack worldMatrices = new PoseStack();
        RenderUtil.applyScreenTransform(worldMatrices, isFirstPerson, yawRadians, pitchRadians, config, lookX, lookY, lookZ);
        worldMatrices.scale(scale, scale, scale);
        Matrix4f worldPose = worldMatrices.last().pose();

        for (int i = 0; i < corners.length; i++) {
            Vector3f transformed = worldPose.transformPosition(corners[i], new Vector3f());
            Vec3 world = new Vec3(transformed.x(), transformed.y(), transformed.z()).add(cameraPos);
            Vec3 screen = client.gameRenderer.projectPointToScreen(world);
            screenCorners[i].set(
                    (screen.x + 1.0) * 0.5 * width,
                    (1.0 - screen.y) * 0.5 * height
            );
        }

        matrices.popPose();

        INVENTORY_BUFFER.upload();
        StagedVertexBuffer.ExecuteInfo info = INVENTORY_BUFFER.getExecuteInfo(draw);
        if (info == null) {
            INVENTORY_BUFFER.endFrame();
            return;
        }

        drawInventory(info, pipeline, texture);
        INVENTORY_BUFFER.endFrame();
    }

    private void drawInventory(StagedVertexBuffer.ExecuteInfo info, RenderPipeline pipeline, GpuTextureView texture) {
        Minecraft client = Minecraft.getInstance();
        RenderTarget mainTarget = client.gameRenderer.mainRenderTarget();
        GpuTextureView output = mainTarget.getColorTextureView();
        if (output == null) {
            return;
        }

        float fadeAlpha = SpatialGUI.config.enableFadeAnimation 
            ? Math.min(1.0F, (System.currentTimeMillis() - screenOpenTime) / (float) SpatialGUI.config.fadeDurationMs)
            : 1.0F;
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrixCopy(),
                new Vector4f(fadeAlpha, fadeAlpha, fadeAlpha, fadeAlpha),
                new org.joml.Vector3f(),
                new Matrix4f()
        );

        FilterMode filterMode = SpatialGUI.config.useLinearFiltering ? FilterMode.LINEAR : FilterMode.NEAREST;

        try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Spatial GUI",
                output,
                java.util.Optional.empty(),
                null,
                java.util.OptionalDouble.empty()
        )) {
            //? if > 26.2 {
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
            //? } else {
            // renderPass.setPipeline(pipeline);
            //? }
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            //? if > 26.2 {
            renderPass.setUniform
            //? } else {
//            renderPass.bindTexture
            //? }
                    ("Sampler0", texture, RenderSystem.getSamplerCache().getSampler(
                    AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
                    filterMode, filterMode, SpatialGUI.config.useAnisotropicFiltering
            ));
            renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
            renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());
            renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
        }
    }

    public static Vector2d getInventoryMousePosition(double mouseX, double mouseY) {
        double x0 = screenCorners[0].x, y0 = screenCorners[0].y;
        double x1 = screenCorners[1].x, y1 = screenCorners[1].y;
        double x2 = screenCorners[2].x, y2 = screenCorners[2].y;
        double x3 = screenCorners[3].x, y3 = screenCorners[3].y;

        double minX = Math.min(Math.min(x0, x1), Math.min(x2, x3));
        double maxX = Math.max(Math.max(x0, x1), Math.max(x2, x3));
        double minY = Math.min(Math.min(y0, y1), Math.min(y2, y3));
        double maxY = Math.max(Math.max(y0, y1), Math.max(y2, y3));

        if (mouseX < minX || mouseX > maxX || mouseY < minY || mouseY > maxY) {
            return null;
        }

        double dx1 = x1 - x2, dx2 = x3 - x2;
        double dx3 = x0 - x1 + x2 - x3;
        double dy1 = y1 - y2, dy2 = y3 - y2;
        double dy3 = y0 - y1 + y2 - y3;

        double g, h;

        if (Math.abs(dx3) < 1e-9 && Math.abs(dy3) < 1e-9) {
            g = 0.0;
            h = 0.0;
        } else {
            double denom = dx1 * dy2 - dx2 * dy1;
            if (Math.abs(denom) < 1e-9) {
                return null;
            }
            g = (dx3 * dy2 - dx2 * dy3) / denom;
            h = (dx1 * dy3 - dx3 * dy1) / denom;
        }

        double a = x1 - x0 + g * x1;
        double b = x3 - x0 + h * x3;
        double c = x0;
        double d = y1 - y0 + g * y1;
        double e = y3 - y0 + h * y3;
        double f = y0;

        double A11 = a - mouseX * g, A12 = b - mouseX * h, B1 = mouseX - c;
        double A21 = d - mouseY * g, A22 = e - mouseY * h, B2 = mouseY - f;

        double det = A11 * A22 - A12 * A21;
        if (Math.abs(det) < 1e-9) {
            return null;
        }

        double u = (B1 * A22 - A12 * B2) / det;
        double v = (A11 * B2 - B1 * A21) / det;

        if (u < 0.0 || u > 1.0 || v < 0.0 || v > 1.0) {
            return null;
        }

        return new Vector2d(
                u * inventoryTarget.width,
                (1.0 - v) * inventoryTarget.height
        );
    }
}