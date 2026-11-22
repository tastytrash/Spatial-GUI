package org.tastytrash.spatialGUI.mixin.player;

import com.mojang.blaze3d.vertex.PoseStack;
//? if >26.2 {
/*import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
*///?} else {
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
//?}

//? if >1.21.1 {
import net.minecraft.client.renderer.SubmitNodeCollector;
 //?} else {
/*import net.minecraft.client.renderer.MultiBufferSource;
*///?}
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

//? if >26.2 {
/*@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class HandRendererMixin {
    @ModifyVariable(method = "submitArmWithItem", at = @At("HEAD"), argsOnly = true, name = "inverseArmHeight")
    private float spatialGUI$pokeHand(float inverseArmHeight) {
        return inverseArmHeight + SpatialGUIClient.handPokeOffset();
    }

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$hideShield(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state, float partialTicks, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        if (itemStack.getItem() == Items.SHIELD) {
            var renderer = SpatialGUIClient.renderer();
            if (renderer.shouldCapture() && SpatialGUIClient.isEnabled() && ConfigManager.config().hideShieldInFirstPerson) {
                ci.cancel();
            }
        }
    }
}
*///? } else {
@Mixin(ItemInHandRenderer.class)
public class HandRendererMixin {

    @ModifyVariable(method = {
            //? if <26.2 {
            "renderArmWithItem",
            //?} else {
            /*"submitArmWithItem"
            *///?}
    }, at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private float spatialGUI$pokeHand(float inverseArmHeight) {
        return inverseArmHeight + SpatialGUIClient.handPokeOffset();
    }

    //? if 26.2 {
    /*@Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$hideShield(AbstractClientPlayer player, float frameInterp, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        if (itemStack.getItem() == Items.SHIELD) {
            var renderer = SpatialGUIClient.renderer();
            if (renderer.shouldCapture() && SpatialGUIClient.isEnabled() && ConfigManager.config().hideShieldInFirstPerson) {
                ci.cancel();
            }
        }
    }
    *///?} else if >1.21.1 {
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$hideShield(AbstractClientPlayer player, float frameInterp, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        if (itemStack.getItem() == Items.SHIELD) {
            var renderer = SpatialGUIClient.renderer();
            if (renderer.shouldCapture() && SpatialGUIClient.isEnabled() && ConfigManager.config().hideShieldInFirstPerson) {
                ci.cancel();
            }
        }
    }
    //?} else {
    /*@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$hideShield(AbstractClientPlayer player, float frameInterp, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, MultiBufferSource buffer, int lightCoords, CallbackInfo ci) {
        if (itemStack.getItem() == Items.SHIELD) {
            var renderer = SpatialGUIClient.renderer();
            if (renderer.shouldCapture() && SpatialGUIClient.isEnabled() && ConfigManager.config().hideShieldInFirstPerson) {
                ci.cancel();
            }
        }
    }
    *///?}
}
//?}
