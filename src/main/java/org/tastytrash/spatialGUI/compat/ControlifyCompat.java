package org.tastytrash.spatialGUI.compat;

//? if controlify {
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.controller.input.InputComponent;
//? if >1.21.11 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
 //?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
 //?} else {
/*import net.neoforged.fml.ModList;
*///?}
//?}
import org.tastytrash.spatialGUI.util.MouseHandlerUtil;

public final class ControlifyCompat {
    public static boolean inVirtualMove;

    public static final class LookSettings {
        public float h = 1f, v = 1f;
        public boolean invH, invV;
        public float deadzone = 0.15f;

        public double shape(double raw) {
            double a = Math.abs(raw);
            if (a <= deadzone) return 0;
            double n = Math.min(1.0, (a - deadzone) / (1.0 - deadzone));
            return Math.signum(raw) * n * n;
        }

        public double[] stick(double rx, double ry) {
            return new double[]{shape(rx) * h * (invH ? -1 : 1), shape(ry) * v * (invV ? -1 : 1)};
        }
    }

    public static boolean isVirtualMouseActive() {
        //? if controlify {
        if (loaded()) return Bridge.isVirtualMouseActive();
        //?}
        return false;
    }

    public static void renderVirtualMouse(Object graphics) {
        //? if controlify {
        if (loaded()) Bridge.renderVirtualMouse(graphics);
        //?}
    }

    public static LookSettings readLookSettings() {
        //? if controlify {
        if (loaded()) return Bridge.readLookSettings();
        //?}
        return new LookSettings();
    }

    public static double[] pollLookStick(LookSettings s) {
        //? if controlify {
        if (loaded()) return Bridge.pollLookStick(s);
        //?}
        return null;
    }

    public static void handleFreeLook(boolean crosshairModeActive) {
        inVirtualMove = false;
        if (crosshairModeActive) {
            MouseHandlerUtil.addStickLook(pollLookStick(readLookSettings()));
        } else {
            MouseHandlerUtil.resetLookClock();
        }
    }

    //? if controlify {
    private static boolean loaded() {
        //? if fabric {
        return FabricLoader.getInstance().isModLoaded("controlify");
         //?} else {
        /*return ModList.get().isLoaded("controlify");
        *///?}
    }

    private static final class Bridge {
        private static InputComponent input() {
            return Controlify.instance().getCurrentController()
                    .flatMap(ControllerEntity::input)
                    .orElse(null);
        }

        static boolean isVirtualMouseActive() {
            var handler = Controlify.instance().virtualMouseHandler();
            return handler != null && handler.isVirtualMouseEnabled();
        }

        static void renderVirtualMouse(Object graphics) {
            var handler = Controlify.instance().virtualMouseHandler();
            if (handler == null) return;
            //? if >1.21.11 {
            handler.renderVirtualMouse((GuiGraphicsExtractor) graphics);
             //?} else {
            /*handler.renderVirtualMouse((GuiGraphics) graphics);
            *///?}
        }

        static LookSettings readLookSettings() {
            LookSettings s = new LookSettings();
            InputComponent input = input();
            if (input == null) return s;

            var sens = input.settings().sensitivity;
            s.h = sens.hLookSensitivity;
            s.v = sens.vLookSensitivity;
            //? if >1.21.11 {
            s.invH = sens.hLookInvert;
             //?}
            s.invV = sens.vLookInvert;
            s.deadzone = sens.defaultDeadzone;
            return s;
        }

        static double[] pollLookStick(LookSettings s) {
            InputComponent input = input();
            if (input == null) return null;

            ControllerStateView raw = input.rawStateNow();
            double rx = raw.getAxisState(GamepadInputs.RIGHT_STICK_AXIS_RIGHT) - raw.getAxisState(GamepadInputs.RIGHT_STICK_AXIS_LEFT);
            double ry = raw.getAxisState(GamepadInputs.RIGHT_STICK_AXIS_DOWN) - raw.getAxisState(GamepadInputs.RIGHT_STICK_AXIS_UP);
            return s.stick(rx, ry);
        }
    }
    //?}
}