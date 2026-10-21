package org.tastytrash.spatialGUI.editor;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public final class GizmoGeometry {
    private static final int[][] CUBE_FACES = {{0, 2, 3, 1}, {4, 5, 7, 6}, {0, 4, 6, 2}, {1, 3, 7, 5}, {0, 1, 5, 4}, {2, 6, 7, 3}};
    private static final int PYRAMID_QUADS = 5;

    static final int CUBE_QUADS = CUBE_FACES.length;
    static final int ARROW_QUADS = CUBE_QUADS + PYRAMID_QUADS;

    static final float AXIS_START = 0.05f;
    static final float AXIS_LENGTH = 0.25f;

    private static final float CUBE_HALF = 0.015f;
    private static final float AXIS_THICKNESS = 0.008f;
    private static final float AXIS_END_SIZE = 0.025f;
    private static final float HEAD_LENGTH = 0.07f;

    private GizmoGeometry() {}

    static void addCube(VertexConsumer buffer, Matrix4f pose, int argb) {
        addBox(buffer, pose, CUBE_HALF, CUBE_HALF, CUBE_HALF, argb);
    }

    static void addArrow(VertexConsumer buffer, Matrix4f pose, int argb) {
        float shaftEnd = AXIS_START + AXIS_LENGTH - HEAD_LENGTH;
        float shaftHalf = (shaftEnd - AXIS_START) * 0.5f;
        addBox(buffer, new Matrix4f(pose).translate(AXIS_START + shaftHalf, 0, 0),
                shaftHalf, AXIS_THICKNESS, AXIS_THICKNESS, argb);
        addPyramid(buffer, new Matrix4f(pose).translate(shaftEnd, 0, 0), argb);
    }

    private static void addBox(VertexConsumer buffer, Matrix4f pose, float halfX, float halfY, float halfZ, int argb) {
        for (int[] face : CUBE_FACES) {
            for (int i : face) {
                vertex(buffer, pose,
                        (i & 1) == 0 ? -halfX : halfX,
                        (i & 2) == 0 ? -halfY : halfY,
                        (i & 4) == 0 ? -halfZ : halfZ,
                        argb);
            }
        }
    }

    private static void addPyramid(VertexConsumer buffer, Matrix4f pose, int argb) {
        float s = AXIS_END_SIZE;
        float[][] base = {{0, -s, -s}, {0, s, -s}, {0, s, s}, {0, -s, s}};
        for (int i = 0; i < 4; i++) {
            float[] a = base[i];
            float[] b = base[(i + 1) % 4];
            vertex(buffer, pose, a[0], a[1], a[2], argb);
            vertex(buffer, pose, b[0], b[1], b[2], argb);
            vertex(buffer, pose, HEAD_LENGTH, 0, 0, argb);
            vertex(buffer, pose, HEAD_LENGTH, 0, 0, argb);
        }
        for (int i = 3; i >= 0; i--) {
            vertex(buffer, pose, base[i][0], base[i][1], base[i][2], argb);
        }
    }

    private static void vertex(VertexConsumer buffer, Matrix4f pose, float x, float y, float z, int argb) {
        //? if >1.20.1 {
        buffer.addVertex(pose, x, y, z).setColor(argb);
        //?} else {
        /*buffer.vertex(pose, x, y, z).color(argb >> 16 & 255, argb >> 8 & 255, argb & 255, argb >>> 24).endVertex();
         *///?}
    }
}