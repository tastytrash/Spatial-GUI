package org.tastytrash.spatialGUI.mixin.compat;

//? if >1.21.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

@Pseudo
@Mixin(targets = "it.hurts.shatterbyte.shatterlib.client.particle.ParticleSystem", remap = false)
public class ShatterLibParticleMixin {
    @ModifyVariable(method = "renderScreenParticles", at = @At("HEAD"), argsOnly = true, remap = false, require = 0)
    private static GuiGraphicsExtractor spatialGUI$redirectParticles(GuiGraphicsExtractor graphics) {
        var renderer = SpatialGUIClient.renderer();
        if (renderer == null || !SpatialGUIClient.isEnabled() || !renderer.shouldCapture()) {
            return graphics;
        }
        return renderer.createIsolatedGraphics();
    }
}
//?}
