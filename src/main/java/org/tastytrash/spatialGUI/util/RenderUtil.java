package org.tastytrash.spatialGUI.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.tastytrash.spatialGUI.SpatialGUI;

public final class RenderUtil {
    private RenderUtil() {}

    public static void addQuadVertex(VertexConsumer buffer, Matrix4f pose,
            float x, float y, float z, float u, float v) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(255, 255, 255, SpatialGUI.config.screenAlpha);
    }

    public static void applyScreenTransform(PoseStack matrices, boolean isFirstPerson,
            float yawRadians, float pitchRadians, ScreenTransformConfig config,
            double lookX, double lookY, double lookZ) {
        if (isFirstPerson) {
            matrices.translate(
                    lookX * config.distance + Math.cos(yawRadians) * config.sideOffset,
                    lookY * config.distance + config.heightOffset,
                    lookZ * config.distance + Math.sin(yawRadians) * config.sideOffset
            );
            matrices.mulPose(new Quaternionf()
                    .rotateY(-yawRadians + (float) Math.toRadians(config.yawOffset))
                    .rotateX(-pitchRadians + (float) Math.toRadians(config.pitchOffset))
                    .get(new Matrix4f())
            );
        } else {
            matrices.translate(
                    -Mth.sin(yawRadians) * config.distance + Math.cos(yawRadians) * config.sideOffset,
                    config.heightOffset,
                    Mth.cos(yawRadians) * config.distance + Math.sin(yawRadians) * config.sideOffset
            );
            matrices.mulPose(new Quaternionf()
                    .rotateY(-yawRadians + (float) Math.toRadians(config.yawOffset))
                    .rotateX((float) Math.toRadians(config.pitchOffset))
                    .get(new Matrix4f())
            );
        }
    }

    public record ScreenTransformConfig(float distance, float sideOffset, float heightOffset, float yawOffset, float pitchOffset, float scale) { }

    private static float calculateFovScaleMultiplier(boolean autoScaleByFov) {
        if (!autoScaleByFov) return 1.0f;
        
        float currentFov = Minecraft.getInstance().gameRenderer.mainCamera().getFov();
        float baselineFov = (float) SpatialGUI.config.autoFovTuning.autoScaleBaselineFov;
        float power = (float) SpatialGUI.config.autoFovTuning.autoScaleScreenPower;

        float ratio = currentFov / baselineFov;
        return (float) Math.pow(ratio, power);
    }

    public static ScreenTransformConfig getScreenTransformConfig(boolean isFirstPerson) {
        float fovMultiplier = calculateFovScaleMultiplier(SpatialGUI.config.autoScaleByFov);
        
        if (isFirstPerson) {
            return new ScreenTransformConfig(
                    (float) SpatialGUI.config.firstPersonScreenDistance,
                    (float) SpatialGUI.config.firstPersonScreenSideOffset,
                    (float) SpatialGUI.config.firstPersonScreenHeightOffset,
                    (float) SpatialGUI.config.firstPersonScreenYawOffset,
                    (float) SpatialGUI.config.firstPersonScreenPitchOffset,
                    (float) SpatialGUI.config.firstPersonScreenScale * fovMultiplier
            );
        }

        float thirdPersonFovMultiplier = 1.0f + (fovMultiplier - 1.0f) * (float) SpatialGUI.config.autoFovTuning.autoScaleThirdPersonScreenMultiplier;
        return new ScreenTransformConfig(
                (float) SpatialGUI.config.screenDistance,
                (float) SpatialGUI.config.screenSideOffset,
                (float) SpatialGUI.config.screenHeightOffset,
                (float) SpatialGUI.config.screenYawOffset,
                (float) SpatialGUI.config.screenPitchOffset,
                (float) SpatialGUI.config.screenScale * thirdPersonFovMultiplier
        );
    }
}
