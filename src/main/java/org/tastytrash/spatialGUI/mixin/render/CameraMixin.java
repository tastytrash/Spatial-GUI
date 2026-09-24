package org.tastytrash.spatialGUI.mixin.render;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
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
import org.tastytrash.spatialGUI.util.MouseHandlerUtil;
import org.tastytrash.spatialGUI.util.CameraUtil;

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
    @Unique private static double initialMouseX, initialMouseY;
    @Unique private static boolean hasMouseMovedSinceScreenOpen = false;

    @Unique private static double lastGrabMouseX, lastGrabMouseY;
    @Unique private static float freeLookYaw = 0f, freeLookPitch = 0f;

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void diegeticInventory$modifyCamera(float partialTicks, CallbackInfo ci) {
        var renderer = SpatialGUIClient.renderer();
        boolean isCapturing = renderer.shouldCapture();

        if (isCapturing && this.entity != null && SpatialGUI.config.enabled) {
            TRANSITION_DURATION_MS = SpatialGUI.config.transitionDurationMs;
            boolean isFirstPerson = (SpatialGUIRenderer.isInventoryScreen() ? SpatialGUI.config.firstPersonModeInventory : SpatialGUI.config.firstPersonModeContainers)
                    || SpatialGUIClient.getSwitchedToFirstPersonDueToBlock();
            SpatialGUIClient.setEffectiveFirstPersonMode(isFirstPerson);
            MouseHandlerUtil.updateMouseGrabForFirstPerson(isFirstPerson);

            if (!wasCapturing) {
                initialMouseX = Minecraft.getInstance().mouseHandler.xpos();
                initialMouseY = Minecraft.getInstance().mouseHandler.ypos();
                hasMouseMovedSinceScreenOpen = false;

                if (SpatialGUIRenderer.isCrosshairModeActive()) {
                    lastGrabMouseX = Minecraft.getInstance().mouseHandler.xpos();
                    lastGrabMouseY = Minecraft.getInstance().mouseHandler.ypos();
                    freeLookYaw = 0f;
                    freeLookPitch = 0f;
                }
            }

            if (!isFirstPerson && !SpatialGUIClient.getSwitchedToFirstPersonDueToBlock()) {
                CameraUtil.checkBlockCollision(this.entity);
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
    private CameraTransform calculateCameraTransform(boolean isFirstPerson, float partialTicks) {
        float fovMultiplier = CameraUtil.calculateFovMultiplier();
        float sideOffsetMultiplier = (float) SpatialGUI.config.autoFovTuning.autoScaleSideOffsetMultiplier;
        float distanceMultiplier = (float) SpatialGUI.config.autoFovTuning.autoScaleDistanceMultiplier;

        float fovAdjustment = (1.0f / fovMultiplier) - 1.0f;
        float distanceFovAdjustment = fovAdjustment * distanceMultiplier;
        float sideOffsetFovAdjustment = fovAdjustment * sideOffsetMultiplier;
        
        float distance = isFirstPerson ? 0.0f : Math.clamp((float) SpatialGUI.config.cameraDistance * (1.0f + distanceFovAdjustment), -4, 4);
        float sideOffset = isFirstPerson ? 0.0f : Math.clamp((float) SpatialGUI.config.cameraSideOffset * (1.0f + sideOffsetFovAdjustment), -4, 4);
        float heightOffset = isFirstPerson ? entity.getEyeHeight() : Math.clamp((float) SpatialGUI.config.cameraHeightOffset, -4, 4);

        float yaw, pitch;
        float positionYaw;

        Minecraft client = Minecraft.getInstance();
        
        if (!hasMouseMovedSinceScreenOpen) {
            double mouseDeltaX = Math.abs(client.mouseHandler.xpos() - initialMouseX);
            double mouseDeltaY = Math.abs(client.mouseHandler.ypos() - initialMouseY);
            hasMouseMovedSinceScreenOpen = mouseDeltaX > 1.0 || mouseDeltaY > 1.0;
        }
        
        float nx = hasMouseMovedSinceScreenOpen ? (float) (client.mouseHandler.xpos() / client.getWindow().getScreenWidth()) * 2f - 1f : 0f;
        float ny = hasMouseMovedSinceScreenOpen ? (float) (client.mouseHandler.ypos() / client.getWindow().getScreenHeight()) * 2f - 1f : 0f;

        if (isFirstPerson) {
            float maxPitch = 90;

            if (SpatialGUI.config.disableFirstPersonParallax) {
                smoothedCameraYaw = 0f;
                smoothedCameraPitch = entity.getXRot();
            } else if (SpatialGUIRenderer.isCrosshairModeActive()) {
                double deltaX, deltaY;
                Minecraft mc2 = Minecraft.getInstance();
                //? if >26.2 {
                /*double[] rel = MouseHandlerUtil.resetFreeLookDelta();
                deltaX = rel[0];
                deltaY = rel[1];
                *///?} else {
                double curX = mc2.mouseHandler.xpos();
                double curY = mc2.mouseHandler.ypos();
                deltaX = curX - lastGrabMouseX;
                deltaY = curY - lastGrabMouseY;
                lastGrabMouseX = curX;
                lastGrabMouseY = curY;
                //?}

                double ss = mc2.options.sensitivity().get() * 0.6 + 0.2;
                double sens = (ss * ss * ss);

                double xo = deltaX * sens;
                double yo = deltaY * sens;
                if (mc2.options.invertMouseX().get()) xo = -xo;
                if (mc2.options.invertMouseY().get()) yo = -yo;

                freeLookYaw += (float) xo;
                freeLookYaw = Math.clamp(freeLookYaw, -MAX_YAW_OFFSET, MAX_YAW_OFFSET);

                freeLookPitch += (float) yo;
                float minAllowed = -maxPitch - entity.getXRot();
                float maxAllowed = maxPitch - entity.getXRot();
                freeLookPitch = Math.clamp(freeLookPitch, minAllowed, maxAllowed);

                smoothedCameraYaw = freeLookYaw;
                smoothedCameraPitch = entity.getXRot() + freeLookPitch;
            } else {
                smoothedCameraYaw = nx * MAX_YAW_OFFSET * (float) SpatialGUI.config.firstPersonMouseSensitivityYaw;
                smoothedCameraPitch = entity.getXRot() + ny * MAX_PITCH_OFFSET * (float) SpatialGUI.config.firstPersonMouseSensitivityPitch;
            }

            smoothedCameraPitch = Math.clamp(smoothedCameraPitch, -maxPitch, maxPitch);

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
        freeLookYaw = 0f;
        freeLookPitch = 0f;
        SpatialGUIClient.setSwitchedToFirstPersonDueToBlock(false);
        SpatialGUIClient.setEffectiveFirstPersonMode(false);
        MouseHandlerUtil.updateMouseGrabForFirstPerson(false);
    }

    @Unique private record CameraTransform(Vec3 position, float yaw, float pitch) {}
}
