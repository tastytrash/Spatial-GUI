package org.tastytrash.spatialGUI.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class DrawUtil {
    private DrawUtil() {}

    public static final int BUTTON_HEIGHT = 20;
    public static final int SCREEN_MARGIN = 6;

    public static Button button(int width) {
        return Button.builder(Component.empty(), b -> {}).bounds(0, 0, width, BUTTON_HEIGHT).build();
    }

    public static double cursorX() {
        var window = Minecraft.getInstance().getWindow();
        return MouseHandlerUtil.getPhysicalX(-1) * GuiScaleUtil.scaledWidth(window) / window.getScreenWidth();
    }

    public static double cursorY() {
        var window = Minecraft.getInstance().getWindow();
        return MouseHandlerUtil.getPhysicalY(-1) * GuiScaleUtil.scaledHeight(window) / window.getScreenHeight();
    }

    public static void drawButton(Button button, Component label, int x, int y, boolean active, Object g) {
        button.setMessage(label);
        button.setPosition(x, y);
        button.active = active;
        renderWidget(button, g);
    }

    public static void renderWidget(AbstractWidget widget, Object g) {
        renderWidget(widget, g, (int) cursorX(), (int) cursorY());
    }

    public static void renderWidget(AbstractWidget widget, Object g, int cursorX, int cursorY) {
        //? if >26.3 {
        /*widget.extractRenderState((GuiGraphicsExtractor) g);
        *///?} else if >1.21.11 {
        widget.extractRenderState((GuiGraphicsExtractor) g, cursorX, cursorY, 0f);
        //?} else {
        /*widget.render((GuiGraphicsExtractor) g, cursorX, cursorY, 0f);
         *///?}
        //? if <=1.21.1 {
        /*((GuiGraphicsExtractor) g).flush();
         *///?}
    }

    public static void pushScale(Object g, float scale) {
        var pose = ((GuiGraphicsExtractor) g).pose();
        //? if >=1.21.6 {
        pose.pushMatrix();
        pose.scale(scale, scale);
        //?} else {
        /*pose.pushPose();
        pose.scale(scale, scale, 1f);
        *///?}
    }

    public static void popScale(Object g) {
        var pose = ((GuiGraphicsExtractor) g).pose();
        //? if >1.21.1 {
        pose.popMatrix();
        //?} else {
        /*pose.popPose();
         *///?}
    }

    public static void drawText(Object g, String text, int x, int y) {
        var font = Minecraft.getInstance().font;
        //? if >=26.1.2 {
        ((net.minecraft.client.gui.GuiGraphicsExtractor) g).text(font, text, x, y, UiColors.WHITE, true);
        //?} else if >1.21.11 {
        /*((net.minecraft.client.gui.GuiGraphicsExtractor) g).drawString(font, text, x, y, UiColors.WHITE, true);
         *///?} else {
        /*((net.minecraft.client.gui.GuiGraphicsExtractor) g).drawString(font, text, x, y, UiColors.WHITE);
         *///?}
    }

    public static void drawTranslatedText(Object g, String key, int x, int y) {
        drawText(g, Component.translatable(key).getString(), x, y);
    }

    public static String fitText(String text, int maxWidth) {
        var font = Minecraft.getInstance().font;
        if (font.width(text) <= maxWidth) return text;
        int length = text.length();
        while (length > 0 && font.width(text.substring(0, length) + "...") > maxWidth) length--;
        return text.substring(0, length) + "...";
    }

    public static void fillRect(Object g, int x1, int y1, int x2, int y2, int color) {
        ((net.minecraft.client.gui.GuiGraphicsExtractor) g).fill(x1, y1, x2, y2, color);
    }

    public static void pushLayer(Object g) {
        //? if <=1.21.1 {
        /*var pose = ((GuiGraphicsExtractor) g).pose();
        pose.pushPose();
        pose.translate(0f, 0f, 200f);
        *///?}
    }

    public static void popLayer(Object g) {
        //? if <=1.21.1 {
        /*((GuiGraphicsExtractor) g).pose().popPose();
         *///?}
    }
}