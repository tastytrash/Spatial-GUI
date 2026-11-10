package org.tastytrash.spatialGUI.compat;

import com.mojang.blaze3d.platform.Window;

import java.util.Set;
//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
//?} else if neoforge {
/*import net.neoforged.fml.loading.LoadingModList;
 *///?} else if forge {
/*import net.minecraftforge.fml.loading.LoadingModList;
 *///?}

public class EssentialCompat {

    private EssentialCompat() {}

    private static boolean isModLoaded(String id) {
        //? if fabric {
        return FabricLoader.getInstance().isModLoaded(id);
        //?} else {
        /*return LoadingModList.get().getModFileById(id) != null;
         *///?}
    }

    private static final boolean LOADED = isEssentialLoaded();

    private static boolean isEssentialLoaded() {
        try {
            if (isModLoaded("essential") || isModLoaded("essential-loader") || isModLoaded("essential-container")) return true;
            ClassLoader loader = EssentialCompat.class.getClassLoader();
            return loader.getResource("gg/essential/Essential.class") != null
                    || loader.getResource("gg/essential/api/EssentialAPI.class") != null;
        } catch (RuntimeException | LinkageError e) {
            return true;
        }
    }

    public static boolean isEssentialCaller() {
        if (!LOADED) return false;
        return StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE))
                .walk(s -> s.dropWhile(f -> {
                    String name = f.getDeclaringClass().getName();
                    return name.equals(EssentialCompat.class.getName())
                            || name.equals(Window.class.getName())
                            || name.equals("org.tastytrash.spatialGUI.mixin.screen.WindowMixin");
                }).findFirst())
                .map(f -> f.getDeclaringClass().getName().startsWith("gg.essential."))
                .orElse(false);
    }
}