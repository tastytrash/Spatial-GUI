package org.tastytrash.spatialGUI.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.config.SpatialGUIConfig;
import org.tastytrash.spatialGUI.editor.EditorMode;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;
import java.util.function.UnaryOperator;
//? if >1.21.1 {
import net.minecraft.client.input.KeyEvent;
//?}
//? if <26.3 {
import org.lwjgl.glfw.GLFW;
//?}

//? if fabric {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//?} else if neoforge {
/*import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
*///?} else if forge {
/*import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;
*///?}
//? if fabric && >=26.1 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
 //?} else if fabric {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
*///?}

public class SpatialGUIKeybinds {
    private static KeyMapping openConfig;
    private static KeyMapping reloadProfiles;
    private static KeyMapping openEditor;
    private static KeyMapping toggleMouseFree;
    private static boolean toggleMouseFreeHeld = false;
    private static boolean registered = false;
    private static final String OPEN_CONFIG_KEY = "key.spatial-gui.open_config";
    private static final String RELOAD_PROFILES_KEY = "key.spatial-gui.reload_profiles";
    private static final String OPEN_EDITOR_KEY = "key.spatial-gui.open_editor";
    private static final String TOGGLE_MOUSE_FREE_KEY = "key.spatial-gui.toggle_mouse_free";

    //? if fabric && >1.21.1 {
    public static final KeyMapping.Category SPATIAL_GUI_CATEGORY =
            KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("spatial-gui", "main_category"));
    //?} else if neoforge && >1.21.1 {
    /*public static final KeyMapping.Category SPATIAL_GUI_CATEGORY =
            new KeyMapping.Category(net.minecraft.resources.Identifier.fromNamespaceAndPath("spatial-gui", "main_category"));
    *///?}

    private static KeyMapping createKey(String key) {
        return createKey(key, InputConstants.UNKNOWN.getValue());
    }

    private static KeyMapping createKey(String key, int defaultKey) {
        return new KeyMapping(
                key,
                InputConstants.UNKNOWN.getType(),
                defaultKey,
                //? if >1.21.1 {
                SPATIAL_GUI_CATEGORY
                //?} else {
                /*"key.category.spatial-gui.main_category"
                 *///?}
        );
    }

    //? if >1.21.1 {
    public static boolean matchesToggleMouseFree(KeyEvent event) {
        return toggleMouseFree != null && !toggleMouseFree.isUnbound() && toggleMouseFree.matches(event);
    }
    //?} else {
    /*public static boolean matchesToggleMouseFree(int keyCode, int scanCode) {
        return toggleMouseFree != null && !toggleMouseFree.isUnbound() && toggleMouseFree.matches(keyCode, scanCode);
    }
    *///?}

    public static boolean pressToggleMouseFree() {
        if (!SpatialGUIRenderer.isCrosshairModeActive()) return false;
        if (!toggleMouseFreeHeld) {
            toggleMouseFreeHeld = true;
            EditorMode.toggleMouseFree();
        }
        return true;
    }

    private static boolean isToggleMouseFreeDown(Minecraft mc) {
        //? if <26.3 {
        InputConstants.Key key = InputConstants.getKey(toggleMouseFree.saveString());
        if (key.getType() != InputConstants.Type.KEYSYM) return false;
        //? if >1.21.1 {
        long window = mc.getWindow().handle();
        //?} else {
        /*long window = mc.getWindow().getWindow();
        *///?}
        return GLFW.glfwGetKey(window, key.getValue()) == GLFW.GLFW_PRESS;
        //?} else {
        /*return false;
        *///?}
    }

    private static void tick(Minecraft mc) {
        if (toggleMouseFreeHeld && !isToggleMouseFreeDown(mc)) toggleMouseFreeHeld = false;
        while (reloadProfiles.consumeClick()) {
            String message = ConfigManager.reload();
            if (mc.player != null) {
                //? if 1.21.11 {
                /*mc.player.displayClientMessage(Component.literal(message), true);
                *///?} else {
                mc.player.sendSystemMessage(Component.literal(message));
                //?}
            }
        }
        while (openConfig.consumeClick()) {
            //? if >=26.2 {
            /*Screen screen = SpatialGUIConfig.createScreen(mc.gui.screen());
            *///?} else {
            Screen screen = SpatialGUIConfig.createScreen(mc.screen);
            //?}
            mc.setScreen(screen);
        }
        while (openEditor.consumeClick()) {
            EditorMode.toggle(mc);
        }
    }

    private static KeyMapping[] createKeys() {
        openConfig = createKey(OPEN_CONFIG_KEY);
        reloadProfiles = createKey(RELOAD_PROFILES_KEY);
        openEditor = createKey(OPEN_EDITOR_KEY);
        toggleMouseFree = createKey(TOGGLE_MOUSE_FREE_KEY, InputConstants.KEY_LALT);
        return new KeyMapping[]{openConfig, reloadProfiles, openEditor, toggleMouseFree};
    }

    //? if fabric {
    public static void register() {
        for (KeyMapping key : createKeys()) {
            //? if >=26.1.2 {
            KeyMappingHelper.registerKeyMapping(key);
            //?} else {
            /*KeyBindingHelper.registerKeyBinding(key);
             *///?}
        }
        ClientTickEvents.END_CLIENT_TICK.register(SpatialGUIKeybinds::tick);
    }
    //?} else {
    /*public static void register(RegisterKeyMappingsEvent event) {
        if (registered) return;
        registered = true;

        //? if neoforge && >1.21.1 {
        /^event.registerCategory(SPATIAL_GUI_CATEGORY);
        ^///?}

        for (KeyMapping key : createKeys()) event.register(key);

        //? if neoforge {
        /^NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> tick(Minecraft.getInstance()));
        ^///?} else {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent e) -> {
            if (e.phase == TickEvent.Phase.END) tick(Minecraft.getInstance());
        });
        //?}
    }
    *///?}
}