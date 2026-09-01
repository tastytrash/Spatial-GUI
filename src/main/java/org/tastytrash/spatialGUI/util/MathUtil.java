package org.tastytrash.spatialGUI.util;

public final class MathUtil {
    private MathUtil() {}

    public static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    public static double lerp(double start, double end, float t) {
        return start + (end - start) * t;
    }

    public static float easeOutCubic(float t) {
        return 1 - (float) Math.pow(1 - t, 3);
    }
}
