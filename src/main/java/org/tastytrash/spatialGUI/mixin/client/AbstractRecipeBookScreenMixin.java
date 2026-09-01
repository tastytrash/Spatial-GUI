package org.tastytrash.spatialGUI.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(AbstractRecipeBookScreen.class)
public class AbstractRecipeBookScreenMixin {
    @Shadow @Final private RecipeBookComponent<?> recipeBookComponent;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void diegeticInventory$updateIsRecipeBookOpen(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        SpatialGUIRenderer.isRecipeBookOpen = this.recipeBookComponent.isVisible();
    }
}