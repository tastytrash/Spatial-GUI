package org.tastytrash.spatialGUI.util;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
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

    public static Vector3f[] createScreenCorners(float aspect) {
        float halfWidth = aspect * 0.5F;
        float halfHeight = 0.5F;
        return new Vector3f[] {
                new Vector3f(-halfWidth, -halfHeight, 0.0F),
                new Vector3f(halfWidth, -halfHeight, 0.0F),
                new Vector3f(halfWidth, halfHeight, 0.0F),
                new Vector3f(-halfWidth, halfHeight, 0.0F)
        };
    }

    public static void projectCornersToScreen(Vector3f[] corners, Matrix4f worldPose, Vec3 cameraPos, int width, int height, Vector2d[] screenCorners) {
        for (int i = 0; i < corners.length; i++) {
            Vector3f transformed = worldPose.transformPosition(corners[i], new Vector3f());
            Vec3 world = new Vec3(transformed.x(), transformed.y(), transformed.z()).add(cameraPos);
            Vec3 screen = Minecraft.getInstance().gameRenderer.projectPointToScreen(world);
            screenCorners[i].set(
                    (screen.x + 1.0) * 0.5 * width,
                    (1.0 - screen.y) * 0.5 * height
            );
        }
    }

    public static Vector2d getInventoryMousePosition(double mouseX, double mouseY, Vector2d[] screenCorners, TextureTarget inventoryTarget) {
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
