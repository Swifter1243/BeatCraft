package com.beatcraft.client.resources;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.render.instancing.InstancedMesh;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

@Environment(EnvType.CLIENT)
public class ResourceReloadListener implements ResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Beatcraft.LOGGER.info("Reloading client resources");

        InstancedMesh.cleanupAll();


    }
}
