package org.tastytrash.spatialGUI.mixin.render;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.util.MathUtil;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow private Entity entity;
    @Shadow private Vec3 position;
    @Shadow private float xRot;
    @Shadow private float yRot;
    @Final @Shadow private Quaternionf rotation;

    @Unique private static Vec3 startPos;
    @Unique private static float startYRot, targetYRot;
    @Unique private static long transitionStartTime, TRANSITION_DURATION_MS = 1;
    @Unique private static boolean wasCapturing, isTransitioning;
    @Unique private static final float MAX_YAW_OFFSET = 90f, MAX_PITCH_OFFSET = 180f;
    @Unique private static float smoothedCameraYaw, smoothedCameraPitch;

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void diegeticInventory$modifyCamera(float partialTicks, CallbackInfo ci) {
        var renderer = SpatialGUIClient.renderer();
        boolean isCapturing = renderer.shouldCapture();

        if (isCapturing && this.entity != null && SpatialGUI.config.enabled) {
            TRANSITION_DURATION_MS = SpatialGUI.config.transitionDurationMs;
            boolean isFirstPerson = SpatialGUI.config.firstPersonMode || SpatialGUIClient.getSwitchedToFirstPersonDueToBlock();
            SpatialGUIClient.setEffectiveFirstPersonMode(isFirstPerson);

            if (!isFirstPerson && !SpatialGUIClient.getSwitchedToFirstPersonDueToBlock()) {
                this.checkBlockCollision();
            }

            CameraTransform transform = this.calculateCameraTransform(isFirstPerson, partialTicks);
            Vec3 newTargetPos = transform.position;
            float newTargetYRot = transform.yaw;
            float newTargetXRot = transform.pitch;

            targetYRot = newTargetYRot;

            if (!wasCapturing || !isTransitioning) {
                this.startTransition(renderer, newTargetYRot, newTargetXRot, isFirstPerson);
            }

            this.applyCameraUpdate(newTargetPos, newTargetYRot, newTargetXRot, isFirstPerson);
            this.rotation.rotationYXZ((float) Math.PI - this.yRot * ((float) Math.PI / 180F), -this.xRot * ((float) Math.PI / 180F), 0.0F);
            wasCapturing = true;
        } else {
            this.resetCameraState();
        }
    }

    @Unique
    private void checkBlockCollision() {
        if (Minecraft.getInstance().level == null) return;

        float yawRadians = (float) Math.toRadians(entity.getYRot());
        double camX = entity.getX() + Math.sin(yawRadians) * SpatialGUI.config.cameraDistance + Math.cos(yawRadians) * SpatialGUI.config.cameraSideOffset;
        double camY = entity.getY() + SpatialGUI.config.cameraHeightOffset;
        double camZ = entity.getZ() - Math.cos(yawRadians) * SpatialGUI.config.cameraDistance + Math.sin(yawRadians) * SpatialGUI.config.cameraSideOffset;

        Vec3 playerEyePos = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
        Vec3 targetCamPos = new Vec3(camX, camY, camZ);
        BlockPos blockPos = BlockPos.containing(camX, camY, camZ);

        var blockState = Minecraft.getInstance().level.getBlockState(blockPos);
        var collisionShape = blockState.getCollisionShape(Minecraft.getInstance().level, blockPos);
        boolean isInsideBlock = !blockState.isAir() && !collisionShape.isEmpty() && collisionShape.bounds().move(blockPos).inflate(0.001).contains(targetCamPos);

        var clipContext = new net.minecraft.world.level.ClipContext(playerEyePos, targetCamPos, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, entity);
        boolean pathBlocked = Minecraft.getInstance().level.clip(clipContext).getType() != net.minecraft.world.phys.HitResult.Type.MISS;

        if (isInsideBlock || pathBlocked) {
            SpatialGUIClient.setSwitchedToFirstPersonDueToBlock(true);
        }
    }

    @Unique
    private float calculateFovMultiplier() {
        if (!SpatialGUI.config.autoScaleByFov) return 1.0f;

        float currentFov = Minecraft.getInstance().gameRenderer.mainCamera().getFov();
        float baselineFov = (float) SpatialGUI.config.autoFovTuning.autoScaleBaselineFov;
        
        return currentFov / baselineFov;
    }

    @Unique
    private CameraTransform calculateCameraTransform(boolean isFirstPerson, float partialTicks) {
        float fovMultiplier = calculateFovMultiplier();
        float sideOffsetMultiplier = (float) SpatialGUI.config.autoFovTuning.autoScaleSideOffsetMultiplier;
        float distanceMultiplier = (float) SpatialGUI.config.autoFovTuning.autoScaleDistanceMultiplier;
        
        float fovAdjustment = (1.0f / fovMultiplier) - 1.0f;
        float distanceFovAdjustment = fovAdjustment * distanceMultiplier;
        float sideOffsetFovAdjustment = fovAdjustment * sideOffsetMultiplier;
        
        float distance = isFirstPerson ? 0.0f : Math.clamp((float) SpatialGUI.config.cameraDistance * (1.0f + distanceFovAdjustment), -4, 4);
        float sideOffset = isFirstPerson ? 0.0f : Math.clamp((float) SpatialGUI.config.cameraSideOffset * (1.0f + sideOffsetFovAdjustment), -4, 4);
        float heightOffset = isFirstPerson ? 1.62f : Math.clamp((float) SpatialGUI.config.cameraHeightOffset, -4, 4);

        float yaw, pitch;
        float positionYaw;

        Minecraft client = Minecraft.getInstance();
        float nx = (float) (client.mouseHandler.xpos() / client.getWindow().getScreenWidth()) * 2f - 1f;
        float ny = (float) (client.mouseHandler.ypos() / client.getWindow().getScreenHeight()) * 2f - 1f;

        if (isFirstPerson) {

            if (SpatialGUI.config.disableFirstPersonParallax) {
                smoothedCameraYaw = 0f;
                smoothedCameraPitch = entity.getXRot();
            } else {
                smoothedCameraYaw = nx * MAX_YAW_OFFSET * (float) SpatialGUI.config.firstPersonMouseSensitivityYaw;
                smoothedCameraPitch = entity.getXRot() + ny * MAX_PITCH_OFFSET * (float) SpatialGUI.config.firstPersonMouseSensitivityPitch;
            }

            yaw = entity.getYRot() + smoothedCameraYaw;
            pitch = smoothedCameraPitch;
            positionYaw = yaw;
        } else {

            if (SpatialGUI.config.disableThirdPersonParallax) {
                smoothedCameraYaw = 0f;
                smoothedCameraPitch = 0f;
            } else {
                smoothedCameraYaw = nx * MAX_YAW_OFFSET * (float) SpatialGUI.config.thirdPersonMouseSensitivityYaw;
                smoothedCameraPitch = ny * MAX_PITCH_OFFSET * (float) SpatialGUI.config.thirdPersonMouseSensitivityPitch;
            }

            yaw = entity.getYRot() + smoothedCameraYaw;
            pitch = (float) SpatialGUI.config.cameraTargetPitch + smoothedCameraPitch;
            positionYaw = entity.getYRot();
        }

        float yawRadians = (float) Math.toRadians(positionYaw);
        double entityX = MathUtil.lerp(entity.xOld, entity.getX(), partialTicks);
        double entityY = MathUtil.lerp(entity.yOld, entity.getY(), partialTicks);
        double entityZ = MathUtil.lerp(entity.zOld, entity.getZ(), partialTicks);

        double camX = entityX + Math.sin(yawRadians) * distance + Math.cos(yawRadians) * sideOffset;
        double camY = entityY + heightOffset;
        double camZ = entityZ - Math.cos(yawRadians) * distance + Math.sin(yawRadians) * sideOffset;

        return new CameraTransform(new Vec3(camX, camY, camZ), yaw, pitch);
    }

    @Unique
    private void startTransition(SpatialGUIRenderer renderer, float newTargetYRot, float newTargetXRot, boolean isFirstPerson) {
        Vec3 savedStartPos = renderer.getCameraStartPos();
        if (savedStartPos != null) {
            startPos = savedStartPos;
            startYRot = renderer.getCameraStartYRot();
        } else {
            startPos = new Vec3(position.x, position.y, position.z);
            startYRot = yRot;
        }
        targetYRot = newTargetYRot;
        transitionStartTime = System.currentTimeMillis();
        isTransitioning = true;

        if (isFirstPerson) {
            smoothedCameraYaw = newTargetYRot - entity.getYRot();
            smoothedCameraPitch = newTargetXRot;
        } else {
            smoothedCameraYaw = 0f;
            smoothedCameraPitch = 0f;
        }
    }

    @Unique
    private void applyCameraUpdate(Vec3 newTargetPos, float newTargetYRot, float newTargetXRot, boolean isFirstPerson) {
        if (isFirstPerson) {
            position = newTargetPos;
            yRot = newTargetYRot;
        } else {
            long elapsed = System.currentTimeMillis() - transitionStartTime;
            float skipPercentage = SpatialGUI.config.transitionSkipPercentage / 100.0f;
            float adjustedElapsed = elapsed + (skipPercentage * TRANSITION_DURATION_MS);
            float progress = Math.min(adjustedElapsed / TRANSITION_DURATION_MS, 1.0f);
            float easedProgress = MathUtil.easeOutCubic(progress);

            position = new Vec3(MathUtil.lerp(startPos.x, newTargetPos.x, easedProgress), MathUtil.lerp(startPos.y, newTargetPos.y, easedProgress), MathUtil.lerp(startPos.z, newTargetPos.z, easedProgress));
            targetYRot = newTargetYRot;

            if (progress >= 1.0f) {
                yRot = targetYRot;
            } else {
                yRot = MathUtil.lerp(startYRot, targetYRot, easedProgress);
            }

        }
        xRot = newTargetXRot;
    }

    @Unique
    private void resetCameraState() {
        startPos = null;
        wasCapturing = false;
        isTransitioning = false;
        smoothedCameraYaw = 0f;
        smoothedCameraPitch = 0f;
        SpatialGUIClient.setSwitchedToFirstPersonDueToBlock(false);
        SpatialGUIClient.setEffectiveFirstPersonMode(false);
    }

    @Unique private record CameraTransform(Vec3 position, float yaw, float pitch) {}
}
