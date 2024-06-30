package com.beatcraft;

import com.beatcraft.entity.BeatCraftEntities;
import com.beatcraft.entity.ColorNoteRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class BeatCraftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerEntityRenderers();
    }

    private void registerEntityRenderers() {
        EntityRendererRegistry.register(BeatCraftEntities.COLOR_NOTE, ColorNoteRenderer::new);
    }
}