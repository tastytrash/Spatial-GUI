package org.tastytrash.spatialGUI.render;

import net.minecraft.client.Minecraft;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.config.ConfigManager;
//? if <=1.21.1 {
/*import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.tastytrash.spatialGUI.compat.ShaderCompat;
*///?}

public final class WorldEffectRenderer {
    private WorldEffectRenderer() {}

    //? if >1.21.1 {
    public static void apply(com.mojang.blaze3d.resource.CrossFrameResourcePool pool) {
        String id = ConfigManager.config().worldEffect.chain;
        if (id == null) return;
        Minecraft mc = Minecraft.getInstance();
        var chain = mc.getShaderManager().getPostChain(net.minecraft.resources.Identifier.parse(id), net.minecraft.client.renderer.LevelTargetBundle.MAIN_TARGETS);
        if (chain == null) {
            return;
        }
        //? if >=26.2 {
        /*chain.process(mc.gameRenderer.mainRenderTarget(), pool);
        *///?} else {
        chain.process(mc.getMainRenderTarget(), pool);
        //?}
    }
    //?} else {
    /*private static PostChain chain;
    private static String chainId;
    private static String failedId;
    private static int width;
    private static int height;
    private static TextureTarget depthBackup;
    private static boolean appliedThisFrame;

    public static void newFrame() {
        appliedThisFrame = false;
    }

    public static void apply() {
        String id = ConfigManager.config().worldEffect.chain;
        if (id == null || appliedThisFrame) return;
        if (ShaderCompat.shaderPackActive()) {
            return;
        }
        appliedThisFrame = true;
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        if (!load(mc, main, id)) return;
        if (width != main.width || height != main.height) {
            chain.resize(main.width, main.height);
            width = main.width;
            height = main.height;
        }
        if (depthBackup == null || depthBackup.width != main.width || depthBackup.height != main.height) {
            if (depthBackup != null) depthBackup.destroyBuffers();
            depthBackup = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
        }
        depthBackup.copyDepthFrom(main);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.resetTextureMatrix();
        chain.process(0.0F);
        main.copyDepthFrom(depthBackup);
        main.bindWrite(true);
        RenderSystem.enableDepthTest();
    }

    private static boolean load(Minecraft mc, RenderTarget main, String id) {
        if (id.equals(chainId)) return true;
        if (id.equals(failedId)) return false;
        if (chain != null) chain.close();
        chain = null;
        chainId = null;
        try {
            chain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), main, Identifier.tryParse(id.replace(":", ":shaders/post/") + ".json"));
            chainId = id;
            width = 0;
            height = 0;
            return true;
        } catch (Exception e) {
            failedId = id;
            SpatialGUI.LOGGER.warn("[SpatialGUI/WorldEffect] post chain {} failed to load", id, e);
            return false;
        }
    }
    *///?}
}
