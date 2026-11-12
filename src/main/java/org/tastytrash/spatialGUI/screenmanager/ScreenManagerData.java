package org.tastytrash.spatialGUI.screenmanager;

import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.config.SpatialGUIConfig;

import java.util.*;

public class ScreenManagerData {
    public static final int UNSORTED = 0;
    public static final int ENABLED = 1;
    public static final int DISABLED = 2;
    public static final int COLUMN_COUNT = 3;

    private static final String SUFFIX = "Screen";

    private final List<List<String>> columns = List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    private int selectedColumn = -1;
    private String selectedId;
    private boolean advancedMode;
    private boolean sortById;

    public ScreenManagerData() {
        SpatialGUIConfig config = SpatialGUIConfig.get();
        Set<String> disabled = clean(config.disabledScreens);
        Set<String> enabled = clean(config.enabledScreens);
        Set<String> unsorted = clean(config.seenScreens);
        enabled.removeAll(disabled);
        unsorted.removeAll(enabled);
        unsorted.removeAll(disabled);
        columns.get(UNSORTED).addAll(unsorted);
        columns.get(ENABLED).addAll(enabled);
        columns.get(DISABLED).addAll(disabled);
        sortAll();
    }

    private static Set<String> clean(List<String> ids) {
        Set<String> result = new LinkedHashSet<>(ids);
        result.removeIf(id -> id == null || id.isBlank());
        return result;
    }

    public String displayName(String id) {
        String name = id.substring(id.lastIndexOf('.') + 1);
        if (!advancedMode && name.length() > SUFFIX.length() && name.endsWith(SUFFIX)) {
            return name.substring(0, name.length() - SUFFIX.length());
        }
        return name;
    }

    public boolean isAdvancedMode() {
        return advancedMode;
    }

    public boolean isSortById() {
        return sortById;
    }

    public void toggleAdvancedMode() {
        advancedMode = !advancedMode;
        sortAll();
    }

    public void toggleSortById() {
        sortById = !sortById;
        sortAll();
    }

    public List<String> ids(int column) {
        return Collections.unmodifiableList(columns.get(column));
    }

    public boolean isSelected(int column, String id) {
        return column == selectedColumn && id.equals(selectedId);
    }

    public void select(int column, String id) {
        selectedColumn = column;
        selectedId = id;
    }

    public String getSelectedId() {
        return selectedId;
    }

    public int getSelectedColumn() {
        return selectedColumn;
    }

    public boolean canMove(int direction) {
        int target = selectedColumn + direction;
        return selectedId != null && target >= 0 && target < COLUMN_COUNT;
    }

    public void moveSelected(int direction) {
        if (!canMove(direction)) return;
        int target = selectedColumn + direction;
        columns.get(selectedColumn).remove(selectedId);
        columns.get(target).add(selectedId);
        sort(columns.get(target));
        selectedColumn = target;
    }

    public void save() {
        SpatialGUIConfig config = SpatialGUIConfig.get();
        Set<String> seen = new LinkedHashSet<>(config.seenScreens);
        seen.addAll(columns.get(UNSORTED));
        config.seenScreens = new ArrayList<>(seen);
        config.enabledScreens = new ArrayList<>(columns.get(ENABLED));
        config.disabledScreens = new ArrayList<>(columns.get(DISABLED));
        SpatialGUIConfig.HANDLER.save();
        ConfigManager.clearCache();
    }

    private void sortAll() {
        columns.forEach(this::sort);
    }

    private void sort(List<String> column) {
        if (sortById) {
            Collections.sort(column);
        } else {
            column.sort(Comparator.comparing(this::displayName));
        }
    }
}