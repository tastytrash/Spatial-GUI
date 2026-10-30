package org.tastytrash.spatialGUI.editor;

import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.config.ConfigProfiles;
import org.tastytrash.spatialGUI.config.SpatialGUIConfig;

final class EditorProfiles {
    static final int MAX_ENTRIES = 8;

    private static int selected = 0;

    private EditorProfiles() {}

    static int count() {
        return ConfigManager.profiles().names().size();
    }

    static boolean canAdd() {
        return count() < MAX_ENTRIES;
    }

    static int selected() {
        return selected;
    }

    static String name(int slot) {
        return ConfigManager.profiles().names().get(slot);
    }

    static void begin() {
        selected = getDefaultSlot();
        ConfigManager.setEditorConfig(null);
    }

    static void end() {
        saveCurrent();
        selected = getDefaultSlot();
        ConfigManager.setEditorConfig(null);
    }

    static void select(int slot) {
        if (slot == selected || slot < 0 || slot >= count()) return;

        EditorHistory.checkpoint();
        saveCurrent();

        SpatialGUIConfig target = ConfigManager.profiles().getProfileConfig(name(slot), SpatialGUI.config);
        if (target == null) {
            slot = getDefaultSlot();
            target = SpatialGUI.config;
        }

        selected = slot;
        ConfigManager.setEditorConfig(target);
        EditorHistory.reset();
    }

    static int getDefaultSlot() {
        String linked = ConfigManager.profiles().linked();
        int slot = linked == null ? -1 : ConfigManager.profiles().names().indexOf(linked);
        return Math.max(slot, 0);
    }

    static String label(int slot) {
        return (slot == getDefaultSlot() ? "§e★ §r" : "") + name(slot);
    }

    static boolean canSetDefault() {
        return selected != getDefaultSlot();
    }

    static void setAsDefault() {
        if (!canSetDefault()) return;

        var profiles = ConfigManager.profiles();

        String previous = profiles.linked();
        if (previous != null) profiles.refreshProfile(previous, SpatialGUI.config);

        SpatialGUIConfig edited = ConfigManager.editorConfig();
        if (edited != null && edited != SpatialGUI.config) ConfigProfiles.copyProfileSettings(edited, SpatialGUI.config);
        profiles.setLinked(name(selected));
        profiles.refreshProfile(name(selected), SpatialGUI.config);
        ConfigManager.setEditorConfig(SpatialGUI.config);
        EditorHistory.reset();

        ConfigManager.saveProfiles();
        SpatialGUIConfig.HANDLER.save();
    }

    static int add() {
        if (!canAdd()) return -1;
        ConfigManager.profiles().addNext(SpatialGUI.config);
        ConfigManager.saveProfiles();
        return count() - 1;
    }

    static void rename(int slot, String value) {
        String name = value.strip();
        if (slot < 0 || slot >= count() || name.isEmpty()) return;
        if (ConfigManager.profiles().rename(name(slot), name)) ConfigManager.saveProfiles();
    }

    static boolean canDelete(int slot) {
        return slot >= 0 && slot < count() && count() > 1 && slot != getDefaultSlot();
    }

    static void delete(int slot) {
        if (!canDelete(slot)) return;
        if (!ConfigManager.profiles().remove(name(slot))) return;
        ConfigManager.saveProfiles();

        if (slot == selected) {
            selected = getDefaultSlot();
            ConfigManager.setEditorConfig(SpatialGUI.config);
            EditorHistory.reset();
        } else if (selected > slot) {
            selected--;
        }
    }

    private static void saveCurrent() {
        SpatialGUIConfig edited = ConfigManager.editorConfig();
        if (edited == null || selected < 0 || selected >= count()) return;
        ConfigManager.profiles().refreshProfile(name(selected), edited);
        ConfigManager.saveProfiles();
    }
}