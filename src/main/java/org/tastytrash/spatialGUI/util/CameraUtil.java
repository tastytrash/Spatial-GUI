package org.tastytrash.spatialGUI.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

public class CameraUtil {
    public static void checkCameraCollision(Player player) {
        float thirdPersonDistance = (float) SpatialGUI.config.cameraDistance;
        float thirdPersonSideOffset = (float) SpatialGUI.config.cameraSideOffset;
        float thirdPersonHeightOffset = (float) SpatialGUI.config.cameraHeightOffset;
        float thirdPersonYaw = player.getYRot();
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

    public static void checkBlockCollision(Entity entity) {
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

        var clipContext = new ClipContext(playerEyePos, targetCamPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
        boolean pathBlocked = Minecraft.getInstance().level.clip(clipContext).getType() != HitResult.Type.MISS;

        if (isInsideBlock || pathBlocked) {
            SpatialGUIClient.setSwitchedToFirstPersonDueToBlock(true);
        }
    }

    public static float calculateFovMultiplier() {
        if (!SpatialGUI.config.autoScaleByFov) return 1.0f;

        float currentFov = Minecraft.getInstance().gameRenderer.mainCamera().getFov();
        float baselineFov = (float) SpatialGUI.config.autoFovTuning.autoScaleBaselineFov;

        return currentFov / baselineFov;
    }
}
