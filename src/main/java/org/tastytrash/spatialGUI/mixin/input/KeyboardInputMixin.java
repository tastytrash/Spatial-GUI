package org.tastytrash.spatialGUI.mixin.input;

//? if >1.21.1 {
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.phys.Vec2;
//?} else {
/*import net.minecraft.client.player.Input;
*///?}
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

@Mixin(KeyboardInput.class)
//? if >1.21.1 {
public abstract class KeyboardInputMixin extends ClientInput {
//?} else {
/*public abstract class KeyboardInputMixin extends Input {
*///?}

    @Inject(method = "tick", at = @At("TAIL"))
    private void spatialGUI$cameraRelativeMovement(CallbackInfo ci) {
        var renderer = SpatialGUIClient.renderer();
        if (renderer == null || !SpatialGUIClient.isEnabled() || !renderer.shouldCapture()) return;
        if (renderer.getHookedScreen() == null) return;

        float delta = (float) Math.toRadians(SpatialGUIClient.getCameraYawOffset());
        if (delta == 0f) return;

        float sin = (float) Math.sin(delta);
        float cos = (float) Math.cos(delta);

        //? if >1.21.1 {
        float left = this.moveVector.x;
        float fwd = this.moveVector.y;
        this.moveVector = new Vec2(left * cos - fwd * sin, fwd * cos + left * sin);
        //?} else {
        /*float left = this.leftImpulse;
        float fwd = this.forwardImpulse;
        this.leftImpulse = left * cos - fwd * sin;
        this.forwardImpulse = fwd * cos + left * sin;
        *///?}
    }
}