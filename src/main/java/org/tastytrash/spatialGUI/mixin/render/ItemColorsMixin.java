package org.tastytrash.spatialGUI.mixin.render;

import org.spongepowered.asm.mixin.Mixin;

//? if <=1.21.1 {
/*import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(ItemColors.class)
public class ItemColorsMixin {
    @Inject(method = "getColor", at = @At("RETURN"), cancellable = true)
    private void spatialGUI$forceOpaqueTint(ItemStack stack, int tintIndex, CallbackInfoReturnable<Integer> cir) {
        int color = cir.getReturnValueI();
        if (SpatialGUIRenderer.isExtractingScreen && color != -1 && (color >>> 24) == 0) {
            cir.setReturnValue(color | 0xFF000000);
        }
    }
}
*///?} else {
@Mixin(net.minecraft.client.Minecraft.class)
public class ItemColorsMixin {}
//?}