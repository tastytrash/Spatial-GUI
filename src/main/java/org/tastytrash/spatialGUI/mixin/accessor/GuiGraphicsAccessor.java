package org.tastytrash.spatialGUI.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;

//? if >1.21.1 && <26.1.2 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.state.GuiRenderState;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsAccessor {
    @Mutable
    @Accessor("guiRenderState")
    void spatialGUI$setGuiRenderState(GuiRenderState state);
}
*///?} else {
@Mixin(net.minecraft.client.Minecraft.class)
public interface GuiGraphicsAccessor {}
//?}