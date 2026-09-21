package org.tastytrash.spatialGUI.mixin.gui;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
    @Accessor("mouseGrabbed")
    void setMouseGrabbed(boolean value);

    @Accessor("mouseGrabbed")
    boolean getMouseGrabbed();
}
