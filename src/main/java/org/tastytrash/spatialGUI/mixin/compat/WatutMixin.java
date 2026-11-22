package org.tastytrash.spatialGUI.mixin.compat;

//? if <=1.21.1 {
/*import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.api.SpatialGUIAPI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Pseudo
@Mixin(targets = {
        "com.corosus.watut.PlayerStatusManagerClient",
        "com.corosus.watut.client.screen.RenderHelper"
}, remap = false)
public class WatutMixin {

    @ModifyReturnValue(method = "shouldAnimate", at = @At("RETURN"), remap = false, require = 0)
    private boolean spatialGUI$hideWatutScreen(boolean original, Player player) {
        if (!original) return false;
        var renderer = SpatialGUIClient.renderer();
        if (renderer == null) return original;
        return !((SpatialGUIAPI.isActive() || renderer.isClosing()) && player == Minecraft.getInstance().player);
    }

    @Inject(method = "renderWithTooltipEnd", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void spatialGUI$skipWatutCapture(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (SpatialGUIRenderer.isExtractingScreen) ci.cancel();
    }
}
*///?}