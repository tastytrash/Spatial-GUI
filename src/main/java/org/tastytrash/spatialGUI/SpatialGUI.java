package org.tastytrash.spatialGUI;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tastytrash.spatialGUI.client.SpatialGUIConfig;

public class SpatialGUI implements ModInitializer {
    public static final String MOD_ID = "spatial-gui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static SpatialGUIConfig config;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Spatial GUI");
        AutoConfig.register(SpatialGUIConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(SpatialGUIConfig.class).getConfig();
    }
}
