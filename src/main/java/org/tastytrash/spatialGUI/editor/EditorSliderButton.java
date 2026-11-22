package org.tastytrash.spatialGUI.editor;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
//? if 1.20.1 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
*///?}

import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

public final class EditorSliderButton extends AbstractSliderButton {
    private final Supplier<Component> label;
    private final double min;
    private final double max;
    private final DoubleConsumer onChange;
    private boolean dragging;

    //? if 1.20.1 {
    /*private static final Identifier SLIDER_TEXTURE = new Identifier("textures/gui/slider.png");
    *///?}

    public EditorSliderButton(int width, int height, Supplier<Component> label, double min, double max, DoubleConsumer onChange) {
        super(0, 0, width, height, Component.empty(), 0);
        this.label = label;
        this.min = min;
        this.max = max;
        this.onChange = onChange;
        updateMessage();
    }

    public boolean dragging() {
        return dragging;
    }

    public void sync(double actual) {
        value = Mth.clamp((actual - min) / (max - min), 0, 1);
        updateMessage();
    }

    public void press(double cursorX) {
        dragging = true;
        dragTo(cursorX);
    }

    public void release() {
        dragging = false;
    }

    public void dragTo(double cursorX) {
        value = Mth.clamp((cursorX - getX() - 4) / (getWidth() - 8), 0, 1);
        applyValue();
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(label.get().getString() + ": " + String.format("%.2f", min + value * (max - min))));
    }

    @Override
    protected void applyValue() {
        onChange.accept(min + value * (max - min));
    }

    //? if 1.20.1 {
    /*@Override
    public void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        graphics.blitNineSliced(SLIDER_TEXTURE, getX(), getY(), getWidth(), getHeight(), 20, 4, 200, 20, 0, 0);
        graphics.blitNineSliced(SLIDER_TEXTURE, getX() + (int) (value * (getWidth() - 8)), getY(), 8, getHeight(), 20, 4, 200, 20, 0, isHoveredOrFocused() ? 60 : 40);
        renderScrollingString(graphics, Minecraft.getInstance().font, 2, active ? 0xFFFFFFFF : 0xFFA0A0A0);
    }
    *///?}
}
