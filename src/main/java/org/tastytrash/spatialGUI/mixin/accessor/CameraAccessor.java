package org.tastytrash.spatialGUI.mixin.accessor;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Accessor("xRot") float spatialGUI$getXRot();
    @Accessor("yRot") float spatialGUI$getYRot();
}