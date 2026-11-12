package org.tastytrash.spatialGUI.util;

public final class UiColors {
    public static final int WHITE = 0xFFFFFFFF;
    public static final int BLACK = 0xFF000000;
    public static final int SKY_BLUE = 0xFF33AAFF;
    public static final int RED = 0xFFFF2B2B;
    public static final int NEAR_BLACK = 0xFF101010;
    public static final int DARKEST_GRAY = 0xFF1E1E1E;
    public static final int DARKER_GRAY = 0xFF2C2C2C;
    public static final int DARK_GRAY = 0xFF4A4A4A;
    public static final int LIGHT_GRAY = 0xFFCCCCCC;

    public static final int BLACK_70 = withOpacity(BLACK, 70);
    public static final int NEAR_BLACK_80 = withOpacity(NEAR_BLACK, 80);
    public static final int NEAR_BLACK_95 = withOpacity(NEAR_BLACK, 95);
    public static final int SKY_BLUE_25 = withOpacity(SKY_BLUE, 25);
    public static final int DARK_GRAY_30 = withOpacity(DARK_GRAY, 30);

    private UiColors() {}

    public static int withOpacity(int color, int percent) {
        int alpha = Math.round(percent * 255 / 100f);
        return (alpha << 24) | (color & 0xFFFFFF);
    }
}