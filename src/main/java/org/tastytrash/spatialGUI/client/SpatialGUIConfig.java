package org.tastytrash.spatialGUI.client;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "spatial-gui")
public class SpatialGUIConfig implements ConfigData {
    // general
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    public boolean enabled = true;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    public boolean firstPersonMode = false;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    public boolean autoCalculateGuiScale = true;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 4)
    public int guiScale = 4;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    public boolean autoScaleByFov = true;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.CollapsibleObject
    public AutoFovTuning autoFovTuning = new AutoFovTuning();

    public static class AutoFovTuning {
        @ConfigEntry.Gui.Tooltip
        public double autoScaleBaselineFov = 70.0;

        @ConfigEntry.Gui.Tooltip
        public double autoScaleScreenPower = 1.2;

        @ConfigEntry.Gui.Tooltip
        public double autoScaleSideOffsetMultiplier = -0.3;

        @ConfigEntry.Gui.Tooltip
        public double autoScaleDistanceMultiplier = 0.8;

        @ConfigEntry.Gui.Tooltip
        public double autoScaleThirdPersonScreenMultiplier = 0.82;
    }

    public int calculateAutoGuiScale(int windowHeight) {
        if (windowHeight > 800) {
            return 4;
        } else if (windowHeight >= 650) {
            return 3;
        } else if (windowHeight >= 430) {
            return 2;
        } else {
            return 1;
        }
    }

    // rendering
    @ConfigEntry.Category("rendering")
    @ConfigEntry.Gui.Tooltip
    public boolean useLinearFiltering = true;

    @ConfigEntry.Category("rendering")
    @ConfigEntry.Gui.Tooltip
    public boolean useAnisotropicFiltering = true;


    @ConfigEntry.Category("rendering")
    @ConfigEntry.Gui.Tooltip
    public double recipeBookShrinkFactor = 1.5;

    @ConfigEntry.Category("rendering")
    @ConfigEntry.Gui.Tooltip
    public boolean hideHandsInFirstPerson = false;

    @ConfigEntry.Category("rendering")
    @ConfigEntry.Gui.Tooltip
    public boolean hideShieldInFirstPerson = true;

    @ConfigEntry.Category("rendering")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 255)
    public int screenAlpha = 255;

    // thirdPersonScreen
    @ConfigEntry.Category("thirdPersonScreen")
    @ConfigEntry.Gui.Tooltip
    public double screenDistance = 2.5;

    @ConfigEntry.Category("thirdPersonScreen")
    @ConfigEntry.Gui.Tooltip
    public double screenSideOffset = -0.6;

    @ConfigEntry.Category("thirdPersonScreen")
    @ConfigEntry.Gui.Tooltip
    public double screenHeightOffset = -0.4;

    @ConfigEntry.Category("thirdPersonScreen")
    @ConfigEntry.Gui.Tooltip
    public double screenYawOffset = 160.0;

    @ConfigEntry.Category("thirdPersonScreen")
    @ConfigEntry.Gui.Tooltip
    public double screenPitchOffset = 0.0;

    @ConfigEntry.Category("thirdPersonScreen")
    @ConfigEntry.Gui.Tooltip
    public double screenScale = 3.5;

    // firstPersonScreen
    @ConfigEntry.Category("firstPersonScreen")
    @ConfigEntry.Gui.Tooltip
    public double firstPersonScreenDistance = 1.5;

    @ConfigEntry.Category("firstPersonScreen")
    @ConfigEntry.Gui.Tooltip
    public double firstPersonScreenSideOffset = 0.0;

    @ConfigEntry.Category("firstPersonScreen")
    @ConfigEntry.Gui.Tooltip
    public double firstPersonScreenHeightOffset = 0.0;

    @ConfigEntry.Category("firstPersonScreen")
    @ConfigEntry.Gui.Tooltip
    public double firstPersonScreenYawOffset = 180.0;

    @ConfigEntry.Category("firstPersonScreen")
    @ConfigEntry.Gui.Tooltip
    public double firstPersonScreenPitchOffset = 0.0;

    @ConfigEntry.Category("firstPersonScreen")
    @ConfigEntry.Gui.Tooltip
    public double firstPersonScreenScale = 2.3;

    // thirdPersonCamera
    @ConfigEntry.Category("thirdPersonCamera")
    @ConfigEntry.Gui.Tooltip
    public double cameraDistance = 1.7;

    @ConfigEntry.Category("thirdPersonCamera")
    @ConfigEntry.Gui.Tooltip
    public double cameraSideOffset = -1.2;

    @ConfigEntry.Category("thirdPersonCamera")
    @ConfigEntry.Gui.Tooltip
    public double cameraHeightOffset = 1.5;

    @ConfigEntry.Category("thirdPersonCamera")
    @ConfigEntry.Gui.Tooltip
    public double cameraTargetPitch = 10.0;

    @ConfigEntry.Category("thirdPersonCamera")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 5000)
    public int transitionDurationMs = 300;

    @ConfigEntry.Category("thirdPersonCamera")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int transitionSkipPercentage = 15;

    @ConfigEntry.Category("thirdPersonCamera")
    @ConfigEntry.Gui.Tooltip
    public double avatarBaseYawOffset = 30.0;

    @ConfigEntry.Category("thirdPersonCamera")
    @ConfigEntry.Gui.Tooltip
    public double avatarBodyRotationOffset = 20.0;

    @ConfigEntry.Category("thirdPersonCamera")
    @ConfigEntry.Gui.Tooltip
    public double thirdPersonMouseSensitivityYaw = 0.1;

    @ConfigEntry.Category("thirdPersonCamera")
    @ConfigEntry.Gui.Tooltip
    public double thirdPersonMouseSensitivityPitch = 0.03;

    @ConfigEntry.Category("thirdPersonCamera")
    @ConfigEntry.Gui.Tooltip
    public boolean disableThirdPersonParallax = false;

    // firstPersonCamera
    @ConfigEntry.Category("firstPersonCamera")
    @ConfigEntry.Gui.Tooltip
    public double firstPersonMouseSensitivityYaw = 0.4;

    @ConfigEntry.Category("firstPersonCamera")
    @ConfigEntry.Gui.Tooltip
    public double firstPersonMouseSensitivityPitch = 0.12;

    @ConfigEntry.Category("firstPersonCamera")
    @ConfigEntry.Gui.Tooltip
    public boolean disableFirstPersonParallax = false;

    // animations
    @ConfigEntry.Category("animations")
    @ConfigEntry.Gui.Tooltip
    public boolean enableFadeAnimation = true;

    @ConfigEntry.Category("animations")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 1000)
    public int fadeDurationMs = 150;

    @ConfigEntry.Category("animations")
    @ConfigEntry.Gui.Tooltip
    public boolean enableScaleAnimation = true;

    @ConfigEntry.Category("animations")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 50, max = 1000)
    public int openAnimationDurationMs = 300;

    @ConfigEntry.Category("animations")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public EasingType animationEasing = EasingType.Back;

    public enum EasingType {
        Cubic,
        Elastic,
        Bounce,
        Back,
        Exponential,
        Quadratic,
        Quartic
    }

    @ConfigEntry.Category("animations")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    public int animationStartScalePercent = 75;

}
