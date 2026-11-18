package org.tastytrash.spatialGUI.compat;

import java.lang.reflect.Method;

public final class ShaderCompat {
    private static boolean initialized;
    private static Object api;
    private static Method inUse;

    private ShaderCompat() {}

    public static boolean shaderPackActive() {
        if (!initialized) {
            initialized = true;
            try {
                Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                api = apiClass.getMethod("getInstance").invoke(null);
                inUse = apiClass.getMethod("isShaderPackInUse");
            } catch (Throwable ignored) {
                api = null;
                inUse = null;
            }
        }

        if (inUse == null) return false;

        try {
            return (boolean) inUse.invoke(api);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
