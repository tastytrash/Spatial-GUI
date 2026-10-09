package org.tastytrash.spatialGUI;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import org.tastytrash.spatialGUI.client.SpatialGUIConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//? if fabric {
 import net.fabricmc.api.ModInitializer;
//? } else if neoforge {
/*import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.bus.api.IEventBus;
*///? } else if forge {
/*import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
*///?}

//? if fabric {
 public class SpatialGUI implements ModInitializer {
//? } else if neoforge{
/*@Mod(SpatialGUI.MOD_ID)
public class SpatialGUI {
*///?} else if forge {
/*public class SpatialGUI {
*///?}
    //? if fabric {
     public static final String MOD_ID = "spatial-gui";
    //? } else {
    /*public static final String MOD_ID = "spatial_gui";
    *///? }
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static SpatialGUIConfig config;

    //? if fabric {
     @Override
     public void onInitialize() {
         initCommon();
     }
    //?} else if neoforge {
    /*public SpatialGUI(ModContainer container, IEventBus modBus) {
        initCommon();
        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (modContainer, parentScreen) -> me.shedaniel.autoconfig.AutoConfigClient.getConfigScreen(SpatialGUIConfig.class, parentScreen).get()
        );
        new org.tastytrash.spatialGUI.client.SpatialGUIClient(modBus);
    }
    *///?} else if forge {
    /*public SpatialGUI() {
        initCommon();
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, parent) -> me.shedaniel.autoconfig.AutoConfigClient.getConfigScreen(SpatialGUIConfig.class, parent).get())
        );
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> org.tastytrash.spatialGUI.client.SpatialGUIClient::new);
    }
    *///?}

    public static void initCommon() {
        LOGGER.info("Initializing Spatial GUI");
        AutoConfig.register(SpatialGUIConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(SpatialGUIConfig.class).getConfig();
    }
}