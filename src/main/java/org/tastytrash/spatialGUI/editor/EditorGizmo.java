package org.tastytrash.spatialGUI.editor;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.config.SpatialGUIConfig;
import org.tastytrash.spatialGUI.util.MouseHandlerUtil;
import org.tastytrash.spatialGUI.util.RenderUtil;
import org.tastytrash.spatialGUI.util.RenderUtil.QuadBasis;

public final class EditorGizmo {
    private static final int SCALE_HANDLES = 4;
    private static final int AXIS_X = 4;
    private static final int AXIS_Y = 5;

    public static final int QUADS = SCALE_HANDLES * HandleGeometry.CUBE_QUADS + 2 * HandleGeometry.ARROW_QUADS;

    public static final int SCALE_HANDLE_COLOR = 0xA0FFFFFF;
    public static final int X_HANDLE_COLOR = 0xA0FF5555;
    public static final int Y_HANDLE_COLOR = 0xA055FF55;
    public static final int ACTIVE_COLOR = 0xA0FFD84A;

    private static final float CORNER_HIT_RADIUS = 0.12f;
    private static final float AXIS_PICK = 0.04f;
    private static final double AXIS_GAIN = 0.35;

    private static boolean pressed;
    private static boolean wasPressed;
    private static boolean dragging;
    private static int handle = -1;
    private static float grabRatio;
    private static final Vector2f grab = new Vector2f();

    public static boolean active() {
        var renderer = SpatialGUIClient.renderer();
        return renderer != null && renderer.getHookedScreen() instanceof EditorScreen;
    }

    public static void setPressed(boolean value) {
        pressed = value;
    }

    public static void resetHandle() {
        if (handle < 0) return;
        var config = ConfigManager.config();
        boolean fp = SpatialGUIClient.getEffectiveFirstPersonMode();

        if (handle < SCALE_HANDLES) {
            if (fp) config.firstPersonScreenScale = 1.8;
            else config.screenScale = 3.5;
        } else if (handle == AXIS_X) {
            if (fp) config.firstPersonScreenSideOffset = 0.0;
            else config.screenSideOffset = -0.6;
        } else if (handle == AXIS_Y) {
            if (fp) config.firstPersonScreenHeightOffset = 0.0;
            else config.screenHeightOffset = -0.4;
        }
        SpatialGUIConfig.HANDLER.save();
    }

    public static void update() {
        boolean started = pressed && !wasPressed;
        wasPressed = pressed;

        QuadBasis basis = currentBasis();
        if (basis == null) {
            pressed = false;
            handle = -1;
            endDrag();
            return;
        }

        Vector2f plane = RenderUtil.getQuadPlanePosition(MouseHandlerUtil.getSourceX(), MouseHandlerUtil.getSourceY(), basis);

        if (dragging) {
            if (!pressed) {
                endDrag();
            } else if (plane != null) {
                if (handle < SCALE_HANDLES) scaleTo(plane, basis);
                else moveTo(plane, basis);
            }
            return;
        }

        handle = plane == null ? -1 : pick(plane, basis);
        if (started && handle >= 0) {
            dragging = true;
            grab.set(plane);
            grabRatio = handle < SCALE_HANDLES ? ratio(plane, basis) : 0f;
        }
    }

    private static QuadBasis currentBasis() {
        if (!active()) return null;
        var inventory = SpatialGUIClient.renderer().getInventoryRenderer();
        QuadBasis basis = inventory.getQuadBasis();
        return inventory.getCylinderBasis() == null && basis != null && basis.halfHeight() > 0f ? basis : null;
    }

    private static void endDrag() {
        if (dragging) SpatialGUIConfig.HANDLER.save();
        dragging = false;
    }

    private static int pick(Vector2f plane, QuadBasis basis) {
        float radius = CORNER_HIT_RADIUS * basis.halfHeight();
        for (int i = 0; i < SCALE_HANDLES; i++) {
            if (plane.distanceSquared(corner(i, basis.halfWidth(), basis.halfHeight())) < radius * radius) return i;
        }
        float k = basis.halfHeight() * 2f;
        if (onArrow(plane.x, plane.y, k)) return AXIS_X;
        if (onArrow(plane.y, plane.x, k)) return AXIS_Y;
        return -1;
    }

    private static boolean onArrow(float along, float across, float k) {
        return along >= (HandleGeometry.AXIS_START - AXIS_PICK) * k
                && along <= (HandleGeometry.AXIS_START + HandleGeometry.AXIS_LENGTH + AXIS_PICK) * k
                && Math.abs(across) < AXIS_PICK * k;
    }

    private static Vector2f corner(int i, float halfWidth, float halfHeight) {
        return new Vector2f(i < 2 ? -halfWidth : halfWidth, i % 2 == 0 ? -halfHeight : halfHeight);
    }

    private static float ratio(Vector2f plane, QuadBasis basis) {
        Vector2f corner = corner(handle, basis.halfWidth(), basis.halfHeight());
        return plane.dot(corner) / corner.lengthSquared();
    }

    private static void scaleTo(Vector2f plane, QuadBasis basis) {
        double factor = ratio(plane, basis) / grabRatio;
        var config = ConfigManager.config();
        if (SpatialGUIClient.getEffectiveFirstPersonMode()) {
            config.firstPersonScreenScale = Mth.clamp(config.firstPersonScreenScale * factor, 0.5, 10.0);
        } else {
            config.screenScale = Mth.clamp(config.screenScale * factor, 0.5, 10.0);
        }
    }

    private static void moveTo(Vector2f plane, QuadBasis basis) {
        var config = ConfigManager.config();
        boolean fp = SpatialGUIClient.getEffectiveFirstPersonMode();

        Vector3f need = new Vector3f(basis.right()).mul(plane.x - grab.x).fma(plane.y - grab.y, basis.up());
        Vector3f dir = handle == AXIS_X ? sideDirection(basis.right(), config, fp) : new Vector3f(0, 1, 0);
        nudge(config, fp, need.dot(dir) * AXIS_GAIN);
    }

    private static Vector3f sideDirection(Vector3f right, SpatialGUIConfig config, boolean fp) {
        boolean mirror = !fp && config.mirrorThirdPerson;
        double yawDeg = fp ? config.firstPersonScreenYawOffset : config.screenYawOffset;
        float yawOffset = (float) Math.toRadians(mirror ? -yawDeg : yawDeg);
        float cos = Mth.cos(yawOffset);
        float sin = Mth.sin(yawOffset);
        Vector3f dir = new Vector3f(cos * right.x() - sin * right.z(), 0f, sin * right.x() + cos * right.z());
        return mirror ? dir.negate() : dir;
    }

    private static void nudge(SpatialGUIConfig c, boolean fp, double delta) {
        if (handle == AXIS_X) {
            if (fp) c.firstPersonScreenSideOffset = Mth.clamp(c.firstPersonScreenSideOffset + delta, -3.0, 3.0);
            else c.screenSideOffset = Mth.clamp(c.screenSideOffset + delta, -5.0, 5.0);
        } else {
            if (fp) c.firstPersonScreenHeightOffset = Mth.clamp(c.firstPersonScreenHeightOffset + delta, -3.0, 3.0);
            else c.screenHeightOffset = Mth.clamp(c.screenHeightOffset + delta, -3.0, 3.0);
        }
    }

    public static void addHandles(VertexConsumer buffer, Matrix4f pose, float aspect) {
        addScaleHandles(buffer, pose, aspect);
        addPositionHandles(buffer, pose);
    }

    public static void addScaleHandles(VertexConsumer buffer, Matrix4f pose, float aspect) {
        for (int i = 0; i < SCALE_HANDLES; i++) {
            Vector2f corner = corner(i, aspect * 0.5f, 0.5f);
            Matrix4f handlePose = new Matrix4f(pose).translate(corner.x, corner.y, 0);
            if (i == handle) handlePose.scale(1.5f);
            HandleGeometry.addCube(buffer, handlePose, i == handle ? ACTIVE_COLOR : SCALE_HANDLE_COLOR);
        }
    }

    public static void addPositionHandles(VertexConsumer buffer, Matrix4f pose) {
        HandleGeometry.addArrow(buffer, pose, handle == AXIS_X ? ACTIVE_COLOR : X_HANDLE_COLOR);
        HandleGeometry.addArrow(buffer, new Matrix4f(pose).rotateZ(Mth.HALF_PI), handle == AXIS_Y ? ACTIVE_COLOR : Y_HANDLE_COLOR);
    }
}