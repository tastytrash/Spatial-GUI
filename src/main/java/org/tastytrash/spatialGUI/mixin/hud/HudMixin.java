package org.tastytrash.spatialGUI.mixin.hud;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >1.20.1 {
import net.minecraft.client.DeltaTracker;
//?}

//? if >=26.2 {
/*@Mixin(net.minecraft.client.gui.Hud.class)
 *///?} else {
@Mixin(net.minecraft.client.gui.Gui.class)
//?}
public abstract class HudMixin {
    @Unique private boolean spatialGUI$vanillaHidden;
    //? if >=26.2 {
    /*@Shadow private boolean isHidden;
     *///?}

    //? if >=26.1.2 {
    @Shadow protected abstract void extractCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
    @Unique private void spatialGUI$drawCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!spatialGUI$vanillaHidden && !SpatialGUIClient.shouldHideCrosshair()) extractCrosshair(graphics, deltaTracker);
    }
    //?} else if >1.20.1 {
    /*@Shadow protected abstract void renderCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
    @Unique private void spatialGUI$drawCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!spatialGUI$vanillaHidden && !SpatialGUIClient.shouldHideCrosshair()) renderCrosshair(graphics, deltaTracker);
    }
    *///?} else {
    /*@Shadow protected abstract void renderCrosshair(GuiGraphicsExtractor graphics);
    @Unique private void spatialGUI$drawCrosshair(GuiGraphicsExtractor graphics, float deltaTracker) {
        if (!spatialGUI$vanillaHidden && !SpatialGUIClient.shouldHideCrosshair()) renderCrosshair(graphics);
    }
    *///?}

    //? if >=26.1.2 {
    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    //?} else {
    /*@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
     *///?}
    private void spatialGUI$hideCrosshair(CallbackInfo ci) {
        if (SpatialGUIClient.shouldHideCrosshair()) {
            ci.cancel();
        }
    }

    @Inject(method = {
            //? if neoforge && 1.21.11 {
            /*"renderHotbar", "renderItemHotbar", "renderHealthLevel", "renderArmorLevel",
            "renderFoodLevel", "renderAirLevel", "renderVehicleHealth", "renderContextualInfoBarBackground",
            "renderExperienceLevel", "renderContextualInfoBar", "renderSelectedItemName", "renderEffects",
            "renderBossOverlay"
            *///?} else if neoforge && >=26.1.2 {
            /*"extractHotbar", "extractHealthLevel", "extractVehicleHealth",
            "extractContextualInfoBarBackground", "extractExperienceLevel", "extractContextualInfoBar",
            "maybeExtractSelectedItemName", "maybeExtractSpectatorTooltip", "extractEffects",
            "extractBossOverlay"
            *///?} else if >=26.1.2 {
            "extractHotbarAndDecorations", "extractEffects", "extractBossOverlay"
            //?} else if >1.21.1 {
            /*"renderHotbarAndDecorations", "renderEffects", "renderBossOverlay"
             *///?} else if >1.20.1 {
            /*"renderItemHotbar", "renderPlayerHealth", "renderVehicleHealth",
            "renderExperienceBar", "renderExperienceLevel", "renderJumpMeter", "renderSelectedItemName"
            *///?} else {
            /*"renderHotbar", "renderPlayerHealth", "renderVehicleHealth",
            "renderExperienceBar", "renderJumpMeter", "renderSelectedItemName"
            *///?}
    }, at = @At("HEAD"), cancellable = true
            //? if <=1.21.1 {
            /*, require = 0
            *///?}
    )
    private void spatialGUI$hideHotbar(CallbackInfo ci) {
        if (SpatialGUIClient.shouldHideHotbar()) {
            ci.cancel();
        }
    }

    //? if (neoforge && (1.21.11 || >=26.1.2)) || 1.21.1 {
    /*//? if >=26.1.2 {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$hideHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    //?} else {
    /^@Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$hideHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    ^///?}
        if (SpatialGUIClient.shouldHideHud()) {
            //? if 1.21.1 {
            /^org.tastytrash.spatialGUI.render.SpatialGUIRenderer.skipWindowOverride = false;
            ^///?}
            //? if >=26.2 {
            /^spatialGUI$vanillaHidden = isHidden;
             ^///?} else {
            spatialGUI$vanillaHidden = Minecraft.getInstance().options.hideGui;
            //?}
            spatialGUI$drawCrosshair(graphics, deltaTracker);
            ci.cancel();
        }
    }
    *///?} else {
    //? if >=26.2 {
    /*@ModifyExpressionValue(method = "extractRenderState", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Hud;isHidden:Z"))
     *///?} else if >=26.1.2 {
    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z"))
    //?} else {
    /*@ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z"))
     *///?}
    private boolean spatialGUI$hideHud(boolean original) {
        spatialGUI$vanillaHidden = original;
        return original || SpatialGUIClient.shouldHideHud();
    }

    //? if >=26.1.2 {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void spatialGUI$restoreCrosshairWhileHidden(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    //?} else if >1.20.1 {
    /*@Inject(method = "render", at = @At("RETURN"))
    private void spatialGUI$keepCrosshairWhileHidden(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    *///?} else {
    /*@Inject(method = "render", at = @At("RETURN"))
    private void spatialGUI$keepCrosshairWhileHidden(GuiGraphicsExtractor graphics, float deltaTracker, CallbackInfo ci) {
    *///?}
        if (SpatialGUIClient.shouldHideHud()) spatialGUI$drawCrosshair(graphics, deltaTracker);
    }
    //?}

    //? if >=26.1.2 {
    @Inject(method = {"extractDebugOverlay", "extractDeferredSubtitles"}, at = @At("HEAD"), cancellable = true)
    //?} else if >1.21.1 {
    /*@Inject(method = {"renderDebugOverlay", "renderSubtitleOverlay"}, at = @At("HEAD"), cancellable = true)
     *///?}
    //? if >1.21.1 {
    private void spatialGUI$hideHudOverlays(CallbackInfo ci) {
        if (SpatialGUIClient.shouldHideHud()) {
            ci.cancel();
        }
    }
    //?}
}