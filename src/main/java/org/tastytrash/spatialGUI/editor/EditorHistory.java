package org.tastytrash.spatialGUI.editor;

import java.util.ArrayDeque;
import java.util.Deque;
import org.tastytrash.spatialGUI.config.ConfigManager;

public final class EditorHistory {
    private static final int MAX_STEPS = 100;
    private static final long SCROLL_SETTLE_MS = 400;

    private record Snapshot(double fpScale, double scale, double fpSide, double side,
            double fpHeight, double height, double fpDistance, double distance) {

        static Snapshot capture() {
            var c = ConfigManager.config();
            return new Snapshot(
                    c.firstPersonScreenScale, c.screenScale,
                    c.firstPersonScreenSideOffset, c.screenSideOffset,
                    c.firstPersonScreenHeightOffset, c.screenHeightOffset,
                    c.firstPersonScreenDistance, c.screenDistance);
        }

        void apply() {
            var c = ConfigManager.config();
            c.firstPersonScreenScale = fpScale;
            c.screenScale = scale;
            c.firstPersonScreenSideOffset = fpSide;
            c.screenSideOffset = side;
            c.firstPersonScreenHeightOffset = fpHeight;
            c.screenHeightOffset = height;
            c.firstPersonScreenDistance = fpDistance;
            c.screenDistance = distance;
        }
    }

    private static final Deque<Snapshot> undoStack = new ArrayDeque<>();
    private static final Deque<Snapshot> redoStack = new ArrayDeque<>();
    private static Snapshot last;
    private static long lastScrollMs;

    private EditorHistory() {}

    public static void reset() {
        undoStack.clear();
        redoStack.clear();
        last = Snapshot.capture();
        lastScrollMs = 0;
    }

    public static void checkpoint() {
        if (last == null) return;
        Snapshot now = Snapshot.capture();
        if (now.equals(last)) return;
        undoStack.push(last);
        if (undoStack.size() > MAX_STEPS) undoStack.removeLast();
        redoStack.clear();
        last = now;
        lastScrollMs = 0;
    }

    public static void tick() {
        if (lastScrollMs != 0 && System.currentTimeMillis() - lastScrollMs > SCROLL_SETTLE_MS) {
            checkpoint();
        }
    }

    public static void undo() {
        if (EditorGizmo.isDragging()) return;
        checkpoint();
        if (undoStack.isEmpty()) return;
        redoStack.push(last);
        last = undoStack.pop();
        last.apply();
    }

    public static void redo() {
        if (EditorGizmo.isDragging()) return;
        checkpoint();
        if (redoStack.isEmpty()) return;
        undoStack.push(last);
        last = redoStack.pop();
        last.apply();
    }

    public static void undoAll() {
        if (EditorGizmo.isDragging()) return;
        checkpoint();
        if (undoStack.isEmpty()) return;
        redoStack.push(last);
        while (undoStack.size() > 1) redoStack.push(undoStack.pop());
        last = undoStack.pop();
        last.apply();
    }

    public static boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public static boolean canRedo() {
        return !redoStack.isEmpty();
    }
}