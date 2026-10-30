package org.tastytrash.spatialGUI.editor;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.tastytrash.spatialGUI.util.EditorUtil;
//? if >1.21.1 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
//?}

final class EditorDropdown {
    static final int BUTTON_WIDTH = 140;
    private static final int DELETE_WIDTH = 40;

    private static final Button header = EditorUtil.button(BUTTON_WIDTH);
    private static final Button addButton = EditorUtil.button(BUTTON_WIDTH);
    private static final Button deleteButton = EditorUtil.button(DELETE_WIDTH);
    private static final Button option = EditorUtil.button(BUTTON_WIDTH);
    private static final EditBox input = new EditBox(Minecraft.getInstance().font, 0, 0, BUTTON_WIDTH - DELETE_WIDTH, EditorUtil.BUTTON_HEIGHT, Component.empty());

    private static boolean open;
    private static int editing = -1;
    private static boolean confirmingDelete;
    private static int xPos;

    private EditorDropdown() {}

    static void close() {
        open = false;
        stopEditing();
    }

    private static String label(int index) {
        return "(" + (index + 1) + ") " + EditorProfiles.label(index);
    }

    private static void startEditing(int index) {
        editing = index;
        input.setValue(EditorProfiles.name(index));
        input.setFocused(true);
    }

    private static void stopEditing() {
        editing = -1;
        confirmingDelete = false;
        input.setFocused(false);
    }

    private static void commitEdit() {
        if (editing >= 0) EditorProfiles.rename(editing, input.getValue());
        stopEditing();
    }

    private static int rowAt(double cursorX, double cursorY) {
        if (cursorX < xPos || cursorX >= xPos + BUTTON_WIDTH) return -2;
        return (int) Math.floor((cursorY - EditorUtil.SCREEN_MARGIN) / EditorUtil.BUTTON_HEIGHT) - 1;
    }

    static void draw(Object g, int guiWidth) {
        boolean usable = EditorMode.cursorFree();
        int y = EditorUtil.SCREEN_MARGIN;
        int selected = EditorProfiles.selected();
        int count = EditorProfiles.count();
        xPos = guiWidth - EditorUtil.SCREEN_MARGIN - BUTTON_WIDTH;

        EditorUtil.drawButton(header, Component.literal(label(selected) + (open ? " ▲" : " ▼")), xPos, y, usable, g);
        if (!open) return;

        for (int i = 0; i < count; i++) {
            int rowY = y + (i + 1) * EditorUtil.BUTTON_HEIGHT;
            if (i == editing) {
                input.setPosition(xPos, rowY);
                EditorUtil.renderWidget(input, g);

                Component deleteLabel = confirmingDelete
                        ? Component.translatable("spatial-gui.editor.dropdown.confirm_delete")
                        : Component.literal("§cx");
                EditorUtil.drawButton(deleteButton, deleteLabel, xPos + BUTTON_WIDTH - DELETE_WIDTH, rowY, usable && EditorProfiles.canDelete(i), g);
            } else {
                EditorUtil.drawButton(option, Component.literal(label(i)), xPos, rowY, usable && i != selected, g);
            }
        }

        if (EditorProfiles.canAdd()) {
            Component addLabel = Component.translatable("spatial-gui.editor.dropdown.add_profile");
            EditorUtil.drawButton(addButton, addLabel, xPos, y + (count + 1) * EditorUtil.BUTTON_HEIGHT, usable && editing < 0, g);
        }
    }

    static boolean click(double cursorX, double cursorY, int mouseButton) {
        if (editing >= 0) {
            if (input.isMouseOver(cursorX, cursorY)) return true;
            if (EditorProfiles.canDelete(editing) && deleteButton.isMouseOver(cursorX, cursorY)) {
                if (confirmingDelete) {
                    EditorProfiles.delete(editing);
                    stopEditing();
                } else {
                    confirmingDelete = true;
                }
                return true;
            }
            commitEdit();
            return true;
        }

        boolean left = mouseButton == InputConstants.MOUSE_BUTTON_LEFT;
        if (left && header.isMouseOver(cursorX, cursorY)) {
            open = !open;
            return true;
        }
        if (!open) return false;

        int row = rowAt(cursorX, cursorY);
        if (row >= 0 && row < EditorProfiles.count()) {
            if (left) {
                EditorProfiles.select(row);
                open = false;
            } else if (mouseButton == InputConstants.MOUSE_BUTTON_RIGHT) {
                startEditing(row);
            }
            return true;
        }
        if (row == EditorProfiles.count() && EditorProfiles.canAdd()) {
            if (left) {
                int index = EditorProfiles.add();
                if (index >= 0) startEditing(index);
            }
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