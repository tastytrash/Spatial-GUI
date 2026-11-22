
package org.tastytrash.spatialGUI.compat;

//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
//?} else if neoforge {
/*import net.neoforged.fml.ModList;
 *///?} else {
/*import net.minecraftforge.fml.ModList;
 *///?}


public final class SpatialGUIMultiplayerCompat {
    private static Boolean isSGMLoaded = null;

    public static boolean isSGMLoaded() {
        if (isSGMLoaded != null) {
            return isSGMLoaded;
        }

        //? if fabric {
        isSGMLoaded = FabricLoader.getInstance().isModLoaded("spatial_gui_multiplayer");
        //?} else if neoforge {
        /*isSGMLoaded = ModList.get().isLoaded("spatial_gui_multiplayer");
         *///?} else {
        /*isSGMLoaded = ModList.get().isLoaded("spatial_gui_multiplayer");
         *///?}

        return isSGMLoaded;
    }
}
