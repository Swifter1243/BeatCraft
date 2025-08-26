package com.beatcraft.client.resources;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.BeatmapManager;
import com.beatcraft.client.lightshow.environment.kaleidoscope.RingSpike;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.beatcraft.client.render.HUDRenderer;
import com.beatcraft.client.render.effect.Bloomfog;
import com.beatcraft.client.render.gl.GlUtil;
import com.beatcraft.client.render.instancing.InstancedMesh;
import com.beatcraft.client.render.instancing.lightshow.light_object.LightMesh;
import com.beatcraft.client.render.mesh.MeshLoader;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

@Environment(EnvType.CLIENT)
public class ResourceReloadListener implements ResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Beatcraft.LOGGER.info("Reloading client resources");

        InstancedMesh.cleanupAll();
        LightMesh.cleanupAll();
        GlUtil.clear();

        Bloomfog.initShaders();
        // TODO: initialize audio systems

        var window = Minecraft.getInstance().getWindow();
        var w = Math.max(1, window.getWidth());
        var h = Math.max(1, window.getHeight());

        if (BeatcraftRenderer.bloomfog == null) BeatcraftRenderer.init();
        BeatcraftRenderer.bloomfog.resize(w, h, true);

        // TODO: load song and replay info from local system AND request from server
        BeatmapManager.loadBeatmaps();

        MeshLoader.loadMeshes();

        // TODO: re-sync music tracks that were playing

        LightMesh.initialized = false;

        RenderSystem.recordRenderCall(() -> {
            LightMesh.buildMeshes();
            RingSpike.reload();
        });


    }
}
