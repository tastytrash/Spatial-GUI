package org.tastytrash.spatialGUI.util;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.util.Mth;
import org.tastytrash.spatialGUI.SpatialGUI;

public final class GuiScaleUtil {
    public static int effectiveScale(Window w) {
        return SpatialGUI.config.getEffectiveGuiScale(w.getWidth(), w.getHeight());
    }
    public static int scaledWidth(Window w)  {
        return Mth.ceil(w.getWidth()  / (double) effectiveScale(w));
    }
    public static int scaledHeight(Window w) {
        return Mth.ceil(w.getHeight() / (double) effectiveScale(w));
    }
}