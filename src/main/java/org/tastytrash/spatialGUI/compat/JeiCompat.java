package org.tastytrash.spatialGUI.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.config.SpatialGUIConfig;

import java.util.Optional;

/** Sets JEI's ingredient and bookmark lists alignment config values, only runs once. */
public final class JeiCompat {
    private static final String HORIZONTAL = "mezz.jei.api.gui.placement.HorizontalAlignment";
    private static final String VERTICAL = "mezz.jei.api.gui.placement.VerticalAlignment";

    private JeiCompat() {}

    public static void tick(Minecraft mc) {
        if (SpatialGUI.config.jeiAlignmentChecked || mc.player == null || !SpatialGUIClient.isEnabled()) {
            return;
        }

        if (mc.player.tickCount % 20 != 0) {
            return;
        }

        try {
            alignLists(mc);
        } catch (Throwable ignored) {
        }
    }

    private static void alignLists(Minecraft mc) throws ReflectiveOperationException {
        Object internalConfigs = Class.forName("mezz.jei.common.Internal")
                .getMethod("getOptionalClientConfigs")
                .invoke(null);

        Object config = ((Optional<?>) internalConfigs).orElse(null);
        if (config == null) return;

        SpatialGUI.config.jeiAlignmentChecked = true;
        SpatialGUIConfig.HANDLER.save();

        Class<?> clientConfigsClass = Class.forName("mezz.jei.common.config.IClientConfigs");
        boolean changed = false;

        // Align lists to LEFT/RIGHT and CENTER
        try {
            Object ingredientConfig = clientConfigsClass.getMethod("getIngredientListConfig").invoke(config);
            if (setAlignment(ingredientConfig, "horizontalAlignment", HORIZONTAL, "LEFT")) changed = true;
            if (setAlignment(ingredientConfig, "verticalAlignment", VERTICAL, "CENTER")) changed = true;

            Object bookmarkConfig = clientConfigsClass.getMethod("getBookmarkListConfig").invoke(config);
            if (setAlignment(bookmarkConfig, "horizontalAlignment", HORIZONTAL, "RIGHT")) changed = true;
            if (setAlignment(bookmarkConfig, "verticalAlignment", VERTICAL, "CENTER")) changed = true;

        } finally {
            if (changed) {
                sendMessage(mc, Component.translatable("spatial-gui.jei.alignment_moved"));
            }
        }
    }

    private static boolean setAlignment(Object gridConfig, String getterMethodName, String enumClassName, String enumValueName) throws ReflectiveOperationException {
        Object configValueObj = Class.forName("mezz.jei.common.config.IIngredientGridConfig")
                .getMethod(getterMethodName)
                .invoke(gridConfig);

        Class<?> configValueClass = Class.forName("net.mezzdev.config.api.value.IConfigValue");

        Object targetEnum = Class.forName(enumClassName).getField(enumValueName).get(null);

        Object currentValue = configValueClass.getMethod("get").invoke(configValueObj);
        if (targetEnum.equals(currentValue)) {
            return false;
        }

        configValueClass.getMethod("set", Object.class).invoke(configValueObj, targetEnum);
        return true;
    }

    private static void sendMessage(Minecraft mc, Component message) {
        //? if 1.21.11 {
        /*mc.player.displayClientMessage(message, false);
         *///?} else {
        mc.player.sendSystemMessage(message);
        //?}
    }
}