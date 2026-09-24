package org.tastytrash.spatialGUI.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.tastytrash.spatialGUI.util.RenderUtil.QuadBasis;
//? > 26.2 {
/*import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.vertex.VertexFormat;
*///? } else {
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
//? }
import org.joml.Vector2d;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.util.AnimationUtil;
import org.tastytrash.spatialGUI.util.MathUtil;
import org.tastytrash.spatialGUI.util.RenderUtil;

public class InventoryRenderer {
    private static final RenderPipeline INVENTORY_PIPELINE = RenderPipelines.GUI_TEXTURED;
    private static final StagedVertexBuffer INVENTORY_BUFFER = new StagedVertexBuffer(
            () -> "Spatial GUI Inventory Buffer",
            RenderType.SMALL_BUFFER_SIZE
    );

    private final TextureTargetManager targetManager;
    private QuadBasis quadBasis;
    private long screenOpenTime = 0;
    private static boolean isRecipeBookOpen = false;
    private static int recipeBookCloseDelay = 0;

    public InventoryRenderer(TextureTargetManager targetManager) {
        this.targetManager = targetManager;
    }

    public QuadBasis getQuadBasis() {
        return quadBasis;
    }

    public void setScreenOpenTime(long time) {
        this.screenOpenTime = time;
    }

    public void setRecipeBookOpen(boolean open) {
        isRecipeBookOpen = open;
    }

    public void renderInWorld(PoseStack matrices) {
        Minecraft client = Minecraft.getInstance();

        if (targetManager.getInventoryTarget() == null || !SpatialGUIClient.renderer().shouldCapture() || client.player == null) {
            return;
        }

        var texture = targetManager.getInventoryTarget().getColorTextureView();
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
        pitch = Math.clamp(pitch, (float) -SpatialGUI.config.firstPersonPitchClamp, (float) SpatialGUI.config.firstPersonPitchClamp);
        float yawRadians = (float) Math.toRadians(yaw);
        float pitchRadians = (float) Math.toRadians(pitch);

        RenderUtil.ScreenTransformConfig config = RenderUtil.getScreenTransformConfig(isFirstPerson);

        double lookX = -Math.sin(yawRadians) * Math.cos(pitchRadians);
        double lookY = -Math.sin(pitchRadians);
        double lookZ = Math.cos(yawRadians) * Math.cos(pitchRadians);

        RenderUtil.applyScreenTransform(matrices, isFirstPerson, yawRadians, pitchRadians, config, lookX, lookY, lookZ);

        float scale = config.scale();
        scale = calculateAnimatedScale(scale);
        matrices.scale(scale, scale, scale);

        Matrix4f pose = matrices.last().pose();
        VertexConsumer buffer = INVENTORY_BUFFER.getVertexBuilder(draw);

        float aspect = (float) targetManager.getInventoryTarget().width / (float) targetManager.getInventoryTarget().height;
        RenderUtil.addScreenQuad(buffer, pose, aspect);

        PoseStack worldMatrices = new PoseStack();
        RenderUtil.applyScreenTransform(worldMatrices, isFirstPerson, yawRadians, pitchRadians, config, lookX, lookY, lookZ);
        worldMatrices.scale(scale, scale, scale);
        Matrix4f worldPose = worldMatrices.last().pose();

        quadBasis = RenderUtil.computeQuadBasis(worldPose, aspect, scale);

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
        var output = mainTarget.getColorTextureView();
        if (output == null) {
            return;
        }

        float fadeAlpha = SpatialGUI.config.enableFadeAnimation
                ? Math.min(1.0F, (System.currentTimeMillis() - screenOpenTime) / (float) SpatialGUI.config.fadeDurationMs)
                : 1.0F;
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrixCopy(),
                new org.joml.Vector4f(fadeAlpha, fadeAlpha, fadeAlpha, fadeAlpha),
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
            //? if >26.2 {
            /*renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
            *///?} else {
            renderPass.setPipeline(pipeline);
             //?}
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            //? if >26.2 {
            /*renderPass.setUniform
                    *///?} else {
                    renderPass.bindTexture
                     //?}
                    ("Sampler0", texture, RenderSystem.getSamplerCache().getSampler(
                            AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
                            filterMode, filterMode, SpatialGUI.config.useAnisotropicFiltering
                    ));
            renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
            renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());
            renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
        }
    }

    private float calculateAnimatedScale(float baseScale) {
        recipeBookCloseDelay = isRecipeBookOpen ? -2 : Math.min(0, recipeBookCloseDelay + 1);
        float scale = (isRecipeBookOpen || recipeBookCloseDelay < 0) ? baseScale / (float) SpatialGUI.config.recipeBookShrinkFactor : baseScale;

        if (SpatialGUI.config.enableScaleAnimation) {
            long elapsed = System.currentTimeMillis() - screenOpenTime;
            float animationProgress = Math.min(1.0F, (float) elapsed / (float) SpatialGUI.config.openAnimationDurationMs);
            float easedProgress = AnimationUtil.applyEasing(SpatialGUI.config.animationEasing, animationProgress);
            float startScalePercent = SpatialGUI.config.animationStartScalePercent / 100.0f;
            float startScale = scale * startScalePercent;
            scale = MathUtil.lerp(startScale, scale, easedProgress);
        }
        return scale;
    }
}
