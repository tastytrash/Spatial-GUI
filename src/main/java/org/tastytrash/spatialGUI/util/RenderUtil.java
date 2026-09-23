package org.tastytrash.spatialGUI.util;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector3f;
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

    public static void addScreenQuad(VertexConsumer buffer, Matrix4f pose, float aspect) {
        float halfWidth = aspect * 0.5F;
        float halfHeight = 0.5F;
        addQuadVertex(buffer, pose, -halfWidth, -halfHeight, 0.0F, 0.0F, 0.0F);
        addQuadVertex(buffer, pose, halfWidth, -halfHeight, 0.0F, 1.0F, 0.0F);
        addQuadVertex(buffer, pose, halfWidth, halfHeight, 0.0F, 1.0F, 1.0F);
        addQuadVertex(buffer, pose, -halfWidth, halfHeight, 0.0F, 0.0F, 1.0F);
    }

    public record QuadBasis(Vector3f centerOffset, Vector3f right, Vector3f up, Vector3f normal, float halfWidth, float halfHeight) {}

    public static QuadBasis computeQuadBasis(Matrix4f worldPose, float aspect, float scale) {
        Vector3f centerOffset = worldPose.transformPosition(new Vector3f(0f, 0f, 0f), new Vector3f());
        Vector3f right = worldPose.transformDirection(new Vector3f(1f, 0f, 0f), new Vector3f()).normalize();
        Vector3f up = worldPose.transformDirection(new Vector3f(0f, 1f, 0f), new Vector3f()).normalize();
        Vector3f normal = new Vector3f(right).cross(up).normalize();
        float halfWidth = aspect * 0.5f * scale;
        float halfHeight = 0.5f * scale;
        return new QuadBasis(centerOffset, right, up, normal, halfWidth, halfHeight);
    }
    public static Vector2d getInventoryMousePositionRay(double screenX, double screenY, QuadBasis basis, TextureTarget inventoryTarget) {
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();

        double ndcX = (screenX / width) * 2.0 - 1.0;
        double ndcY = 1.0 - (screenY / height) * 2.0;

        var camera = mc.gameRenderer.mainCamera();
        float yawRadians = (float) Math.toRadians(camera.yRot());
        float pitchRadians = (float) Math.toRadians(camera.xRot());

        Vector3f forward = new Vector3f(
                (float) (-Math.sin(yawRadians) * Math.cos(pitchRadians)),
                (float) (-Math.sin(pitchRadians)),
                (float) (Math.cos(yawRadians) * Math.cos(pitchRadians))
        ).normalize();

        Vector3f worldUp = new Vector3f(0f, 1f, 0f);
        Vector3f right = new Vector3f(forward).cross(worldUp).normalize();
        Vector3f up = new Vector3f(right).cross(forward).normalize();

        float fovDegrees = camera.getFov();
        float aspect = (float) width / (float) height;
        float tanHalfFovY = (float) Math.tan(Math.toRadians(fovDegrees / 2.0));
        float tanHalfFovX = tanHalfFovY * aspect;

        Vector3f direction = new Vector3f(forward)
                .add(new Vector3f(right).mul((float) ndcX * tanHalfFovX))
                .add(new Vector3f(up).mul((float) ndcY * tanHalfFovY))
                .normalize();

        float denom = direction.dot(basis.normal());
        if (Math.abs(denom) < 1e-6f) {
            return null;
        }

        float t = basis.centerOffset().dot(basis.normal()) / denom;
        if (t <= 0f) {
            return null;
        }

        Vector3f hitOffset = new Vector3f(direction).mul(t);
        Vector3f localOffset = new Vector3f(hitOffset).sub(basis.centerOffset());

        float localX = localOffset.dot(basis.right());
        float localY = localOffset.dot(basis.up());

        float u = (localX / basis.halfWidth() + 1f) / 2f;
        float v = (localY / basis.halfHeight() + 1f) / 2f;

        if (u < 0f || u > 1f || v < 0f || v > 1f) {
            return null;
        }

        return new Vector2d(u * inventoryTarget.width, (1.0 - v) * inventoryTarget.height);
    }
}
