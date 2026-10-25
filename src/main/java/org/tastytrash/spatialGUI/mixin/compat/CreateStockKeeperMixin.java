package org.tastytrash.spatialGUI.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tastytrash.spatialGUI.util.GuiScaleUtil;

@Pseudo
@Mixin(targets = {
        "com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperRequestScreen",
        "com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen"}, remap = false)
public abstract class CreateStockKeeperMixin {
    @Inject(method = "getAppropriateHeight", at = @At("HEAD"), remap = false, require = 0)
    private static void spatialGUI$overrideStart(CallbackInfoReturnable<Integer> cir) {
        GuiScaleUtil.pendingOverride = true;
    }

    @Inject(method = "getAppropriateHeight", at = @At("RETURN"), remap = false, require = 0)
    private static void spatialGUI$overrideEnd(CallbackInfoReturnable<Integer> cir) {
        GuiScaleUtil.pendingOverride = false;
    }
}