package org.tastytrash.spatialGUI.mixin.render;

import org.spongepowered.asm.mixin.Mixin;

//? if <=1.21.1 {
/*import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.injection.At;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {
    @ModifyVariable(method = "_blendFuncSeparate", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private static int spatialGUI$preserveDestAlpha(int dstAlpha) {
        return SpatialGUIRenderer.isExtractingScreen && dstAlpha == GL11.GL_ZERO ? GL11.GL_ONE_MINUS_SRC_ALPHA : dstAlpha;
    }
}
*///?} else {
@Mixin(net.minecraft.client.Minecraft.class)
public class GlStateManagerMixin {}
//?}