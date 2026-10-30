package org.tastytrash.spatialGUI.mixin.screen;

import net.minecraft.client.Minecraft;
//? if >=26.1.2 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?}
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.client.SpatialGUIKeybinds;
import org.tastytrash.spatialGUI.editor.ProfilePicker;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tastytrash.spatialGUI.util.GuiScaleUtil;
//? if >1.21.1 {
import net.minecraft.client.input.KeyEvent;
//?}

//? if >1.20.1 && <26.1.2 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
*///?}


@Mixin(Screen.class)
public class ScreenMixin {
    //? if neoforge {
    /*@Inject(method = "added", at = @At("HEAD"))
    *///?} else {
    @Inject(method = "init()V", at = @At("HEAD"))
    //?}
    private void spatialGUI$hookOnShow(CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        var renderer = SpatialGUIClient.renderer();
        if (renderer != null && SpatialGUIClient.shouldHookScreen(screen)) {
            renderer.hookScreen(screen);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void spatialGUI$markPendingScreen(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        GuiScaleUtil.pendingScreen = SpatialGUIClient.isEnabled() && SpatialGUIClient.shouldHookScreen(self) ? self : null;
    }

    //? if >=26.1.2 {
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$removeBackground(CallbackInfo ci) {
        if (spatialGUI$isHooked()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractTransparentBackground", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$removeTransparentBackground(CallbackInfo ci) {
        if (spatialGUI$isHooked()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractBlurredBackground", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$removeBlur(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (spatialGUI$isHooked()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractMenuBackground", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$removeMenuBackground(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (spatialGUI$isHooked()) {
            ci.cancel();
        }
    }
    //?} else if >1.20.1 {
    /*@Inject(method = "renderBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$removeBackground(CallbackInfo ci) {
        if (SpatialGUIClient.renderer() != null && SpatialGUIClient.renderer().shouldCapture() && SpatialGUIClient.isEnabled()) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "renderTransparentBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fillGradient(IIIIII)V"))
    private void spatialGUI$removeTransparentBackground(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int colorFrom, int colorTo, Operation<Void> original) {
        if (spatialGUI$isHooked()) return;
        original.call(graphics, x1, y1, x2, y2, colorFrom, colorTo);
    }

    @Inject(method = "renderBlurredBackground", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$removeBlurredBackground(CallbackInfo ci) {
        if (SpatialGUIClient.renderer() != null && SpatialGUIClient.renderer().shouldCapture() && SpatialGUIClient.isEnabled()) {
            ci.cancel();
        }
    }
    *///?} else {
    /*@Inject(method = "renderBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$removeBackgroundOverlay(CallbackInfo ci) {
        if (SpatialGUIClient.renderer() != null && SpatialGUIClient.renderer().shouldCapture() && SpatialGUIClient.isEnabled()) {
            if (Minecraft.getInstance().level == null) return;
            ci.cancel();
        }
    }
    *///?}

    //? if >1.21.1 {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$toggleMouseFree(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (spatialGUI$handleProfilePicker(event.key(), SpatialGUIKeybinds.matchesProfilePicker(event))) {
            cir.setReturnValue(true);
            return;
        }
        if (spatialGUI$canToggleMouse() && SpatialGUIKeybinds.matchesToggleMouseFree(event)
                && SpatialGUIKeybinds.pressToggleMouseFree()) {
            cir.setReturnValue(true);
        }
    }
    //?} else {
    /*@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void spatialGUI$toggleMouseFree(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (spatialGUI$handleProfilePicker(keyCode, SpatialGUIKeybinds.matchesProfilePicker(keyCode, scanCode))) {
            cir.setReturnValue(true);
            return;
        }
        if (spatialGUI$canToggleMouse() && SpatialGUIKeybinds.matchesToggleMouseFree(keyCode, scanCode)
                && SpatialGUIKeybinds.pressToggleMouseFree()) {
            cir.setReturnValue(true);
        }
    }
    *///?}

    @Unique
    private boolean spatialGUI$handleProfilePicker(int key, boolean pickerKey) {
        Screen self = (Screen) (Object) this;
        if (ProfilePicker.isOpen()) return ProfilePicker.keyPressed(self, key, pickerKey);
        return pickerKey && spatialGUI$canToggleMouse() && ProfilePicker.open(self);
    }

    @Unique
    private boolean spatialGUI$canToggleMouse() {
        var renderer = SpatialGUIClient.renderer();
        if (renderer == null || !SpatialGUIClient.isEnabled() || renderer.getHookedScreen() != (Object) this) return false;
        GuiEventListener focused = ((Screen) (Object) this).getFocused();
        while (focused instanceof ContainerEventHandler container && container.getFocused() != null) {
            focused = container.getFocused();
        }
        return !(focused instanceof EditBox);
    }

    @Unique
    private boolean spatialGUI$isHooked() {
        var renderer = SpatialGUIClient.renderer();
        return renderer != null
                && SpatialGUIClient.isEnabled()
                && renderer.getHookedScreen() == (Object) this
                && renderer.shouldCapture();
    }
}