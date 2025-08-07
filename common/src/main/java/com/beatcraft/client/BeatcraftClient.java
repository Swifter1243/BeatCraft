package com.beatcraft.client;

import com.beatcraft.client.resources.ResourceReloadListener;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.server.packs.PackType;

public class BeatcraftClient {
    public static void init() {
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, new ResourceReloadListener());
    }

}
