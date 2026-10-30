package org.tastytrash.spatialGUI.editor;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.util.EditorUtil;
import org.tastytrash.spatialGUI.util.GuiScaleUtil;

public final class ProfilePicker {
    private static final int WIDTH = 162;

    private static final Button button = EditorUtil.button(WIDTH);

    private static Screen screen;

    public static boolean isOpen() {
        return screen != null;
    }

    public static boolean open(Screen target) {
        if (target == null || target instanceof EditorScreen) return false;
        screen = target;
        return true;
    }

    public static void close() {
        screen = null;
    }

    private static int currentIndex() {
        String assigned = ConfigManager.profiles().getScreenProfile(screen.getClass().getName());
        int index = assigned == null ? -1 : ConfigManager.profiles().names().indexOf(assigned);
        return index < 0 ? EditorProfiles.getDefaultSlot() : index;
    }

    private static void apply(int index) {
        String profile = index == EditorProfiles.getDefaultSlot() ? null : EditorProfiles.name(index);
        ConfigManager.profiles().setScreenProfile(screen.getClass().getName(), profile);
        ConfigManager.saveProfiles();
        close();
    }

    private static int digitIndex(int key) {
        return switch (key) {
            case InputConstants.KEY_1, InputConstants.KEY_NUMPAD1 -> 0;
            case InputConstants.KEY_2, InputConstants.KEY_NUMPAD2 -> 1;
            case InputConstants.KEY_3, InputConstants.KEY_NUMPAD3 -> 2;
            case InputConstants.KEY_4, InputConstants.KEY_NUMPAD4 -> 3;
            case InputConstants.KEY_5, InputConstants.KEY_NUMPAD5 -> 4;
            case InputConstants.KEY_6, InputConstants.KEY_NUMPAD6 -> 5;
            case InputConstants.KEY_7, InputConstants.KEY_NUMPAD7 -> 6;
            case InputConstants.KEY_8, InputConstants.KEY_NUMPAD8 -> 7;
            case InputConstants.KEY_9, InputConstants.KEY_NUMPAD9 -> 8;
            default -> -1;
        };
    }

    public static boolean keyPressed(Screen target, int key, boolean pickerKey) {
        if (target != screen) {
            close();
            return false;
        }

        int digit = digitIndex(key);

        if (key == InputConstants.KEY_ESCAPE || pickerKey) {
            close();
        } else if (digit >= 0 && digit < EditorProfiles.count()) {
            apply(digit);
        }
        return true;
    }

    public static void drawOverlay(Object g) {
        if (screen == null) return;

        Minecraft client = Minecraft.getInstance();
        Screen current = client.screen;
        if (current != screen) {
            close();
            return;
        }

        int x = client.getWindow().getGuiScaledWidth() - EditorUtil.SCREEN_MARGIN - WIDTH;
        int y = EditorUtil.SCREEN_MARGIN;
        int count = EditorProfiles.count();
        int currentProfile = currentIndex();

        float scale = GuiScaleUtil.overlayScaleRatio(client.getWindow());
        EditorUtil.pushScale(g, scale);

        EditorUtil.drawTranslatedText(g, "spatial-gui.profile_picker.top", x, y + 6);

        for (int i = 0; i < count; i++) {
            button.setMessage(Component.literal((i == currentProfile ? "> (" : "(") + (i + 1) + ") " + EditorProfiles.label(i)));
            button.setPosition(x, y + (i + 1) * EditorUtil.BUTTON_HEIGHT);
            EditorUtil.renderWidget(button, g, -1, -1);
        }

        EditorUtil.drawTranslatedText(g, "spatial-gui.profile_picker.bottom", x, y + (count + 1) * EditorUtil.BUTTON_HEIGHT + 6);

        EditorUtil.popScale(g);

    }
}