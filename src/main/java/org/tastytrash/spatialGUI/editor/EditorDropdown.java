package org.tastytrash.spatialGUI.editor;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
//? if >1.21.1 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
//?}

import java.util.ArrayList;
import java.util.List;

final class EditorDropdown {
    private static final int BUTTON_WIDTH = 140;
    private static final int DELETE_WIDTH = 40;
    private static final int MAX_PROFILES = 8;

    private static final Button header = Button.builder(Component.empty(), b -> {})
            .bounds(0, 0, BUTTON_WIDTH, EditorScreen.BUTTON_HEIGHT).build();
    private static final Button addButton = Button.builder(Component.literal("+ Add profile"), b -> {})
            .bounds(0, 0, BUTTON_WIDTH, EditorScreen.BUTTON_HEIGHT).build();
    private static final Button deleteButton = Button.builder(Component.empty(), b -> {})
            .bounds(0, 0, DELETE_WIDTH, EditorScreen.BUTTON_HEIGHT).build();
    private static final Button[] options = new Button[MAX_PROFILES];
    private static final List<String> names = new ArrayList<>(List.of("Default"));
    private static final EditBox input = new EditBox(Minecraft.getInstance().font, 0, 0, BUTTON_WIDTH - DELETE_WIDTH, EditorScreen.BUTTON_HEIGHT, Component.empty());

    private static boolean open;
    private static int selected = 0;
    private static int editing = -1;
    private static boolean confirmingDelete;

    static {
        for (int i = 0; i < MAX_PROFILES; i++) {
            options[i] = Button.builder(Component.empty(), b -> {})
                    .bounds(0, 0, BUTTON_WIDTH, EditorScreen.BUTTON_HEIGHT).build();
        }
    }

    static void close() {
        open = false;
        stopEditing();
    }

    private static String label(int index) {
        return "(" + (index + 1) + ") " + names.get(index);
    }

    private static boolean canAdd() {
        return names.size() < MAX_PROFILES;
    }

    private static void startEditing(int index) {
        editing = index;
        confirmingDelete = false;
        input.setValue(names.get(index));
        input.setFocused(true);
    }

    private static void stopEditing() {
        editing = -1;
        confirmingDelete = false;
        input.setFocused(false);
    }

    private static void commitEdit() {
        String value = input.getValue().strip();
        if (editing >= 0 && !value.isEmpty()) names.set(editing, value);
        stopEditing();
    }

    private static void addProfile() {
        if (!canAdd()) return;
        names.add("Profile");
        startEditing(names.size() - 1);
    }

    private static void deleteProfile(int index) {
        if (names.size() <= 1) return;
        names.remove(index);
        if (selected > index) selected--;
        selected = Math.min(selected, names.size() - 1);
        stopEditing();
    }

    static void draw(Object g, int guiWidth, int cursorX, int cursorY, boolean usable) {
        int x = guiWidth - EditorScreen.SCREEN_MARGIN - BUTTON_WIDTH;
        int y = EditorScreen.SCREEN_MARGIN;

        header.setMessage(Component.literal(label(selected) + (open ? " ▲" : " ▼")));
        header.setPosition(x, y);
        header.active = usable;
        EditorScreen.renderWidget(header, g, cursorX, cursorY);

        if (!open) return;
        for (int i = 0; i < names.size(); i++) {
            int rowY = y + (i + 1) * EditorScreen.BUTTON_HEIGHT;
            if (i == editing) {
                input.setPosition(x, rowY);
                EditorScreen.renderWidget(input, g, cursorX, cursorY);

                deleteButton.setMessage(Component.literal(confirmingDelete ? "Sure?" : "×").withStyle(ChatFormatting.RED));
                deleteButton.setPosition(x + BUTTON_WIDTH - DELETE_WIDTH, rowY);
                deleteButton.active = usable && names.size() > 1;
                EditorScreen.renderWidget(deleteButton, g, cursorX, cursorY);
            } else {
                options[i].setMessage(Component.literal(label(i)));
                options[i].setPosition(x, rowY);
                options[i].active = usable && i != selected;
                EditorScreen.renderWidget(options[i], g, cursorX, cursorY);
            }
        }

        if (canAdd()) {
            addButton.setPosition(x, y + (names.size() + 1) * EditorScreen.BUTTON_HEIGHT);
            addButton.active = usable && editing < 0;
            EditorScreen.renderWidget(addButton, g, cursorX, cursorY);
        }
    }

    static boolean click(double cursorX, double cursorY, int mouseButton) {
        if (editing >= 0) {
            if (input.isMouseOver(cursorX, cursorY)) return true;
            if (deleteButton.active && deleteButton.isMouseOver(cursorX, cursorY)) {
                if (confirmingDelete) deleteProfile(editing);
                else confirmingDelete = true;
                return true;
            }
            commitEdit();
            return true;
        }

        boolean left = mouseButton == InputConstants.MOUSE_BUTTON_LEFT;
        boolean right = mouseButton == InputConstants.MOUSE_BUTTON_RIGHT;

        if (left && header.isMouseOver(cursorX, cursorY)) {
            open = !open;
            return true;
        }
        if (!open) return false;

        for (int i = 0; i < names.size(); i++) {
            if (!options[i].isMouseOver(cursorX, cursorY)) continue;
            if (left) {
                selected = i;
                open = false;
            } else if (right) {
                startEditing(i);
            }
            return true;
        }

        if (canAdd() && addButton.isMouseOver(cursorX, cursorY)) {
            if (left) addProfile();
            return true;
        }

        open = false;
        return true;
    }

    //? if >1.21.1 {
    static boolean keyPressed(KeyEvent event) {
        int key = event.key();
        //?} else {
        /*static boolean keyPressed(int key, int scanCode, int modifiers) {
         *///?}
        if (editing < 0) return false;

        if (key == InputConstants.KEY_ESCAPE) {
            stopEditing();
        } else if (key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER) {
            commitEdit();
        } else {
            //? if >1.21.1 {
            input.keyPressed(event);
            //?} else {
            /*input.keyPressed(key, scanCode, modifiers);
             *///?}
        }
        return true;
    }

    //? if >1.21.1 {
    static boolean charTyped(CharacterEvent event) {
        if (editing < 0) return false;
        input.charTyped(event);
        return true;
    }
    //?} else {
    /*static boolean charTyped(char character, int modifiers) {
        if (editing < 0) return false;
        input.charTyped(character, modifiers);
        return true;
    }
    *///?}
}