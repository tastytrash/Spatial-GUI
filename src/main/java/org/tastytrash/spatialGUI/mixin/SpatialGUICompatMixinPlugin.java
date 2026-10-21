package org.tastytrash.spatialGUI.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
//?} else if neoforge {
/*import net.neoforged.fml.loading.LoadingModList;
 *///?} else if forge {
/*import net.minecraftforge.fml.loading.LoadingModList;
 *///?}

public class SpatialGUICompatMixinPlugin implements IMixinConfigPlugin {
    private static boolean isModLoaded(String id) {
        //? if fabric {
        return FabricLoader.getInstance().isModLoaded(id);
        //?} else {
        /*return LoadingModList.get().getModFileById(id) != null;
         *///?}
    }

    private static final Map<String, String> OPTIONAL_MIXINS = Map.of(
            "compat.ControlifyMixin", "controlify",
            "compat.ControlifyNeoForgeMixin", "controlify",
            "compat.DynamicCrosshairMixin", "dynamiccrosshair",
            "compat.OwoBlurMixin", "owo",
            "compat.PolytoneMixin", "polytone",
            "compat.SophisticatedCoreMixin", "sophisticatedcore"
    );

    @Override
    public List<String> getMixins() {
        List<String> mixins = new ArrayList<>();
        OPTIONAL_MIXINS.forEach((mixin, modId) -> {
            String path = "org/tastytrash/spatialGUI/mixin/" + mixin.replace('.', '/') + ".class";
            boolean compiled = getClass().getClassLoader().getResource(path) != null;
            if (compiled && isModLoaded(modId)) mixins.add(mixin);
        });
        return mixins;
    }

    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return ""; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return true; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
}