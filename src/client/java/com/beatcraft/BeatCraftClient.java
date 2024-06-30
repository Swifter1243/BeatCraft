package com.beatcraft;

import com.beatcraft.entity.BeatCraftEntities;
import com.beatcraft.entity.ColorNoteRenderer;
import com.beatcraft.model.ColorNoteEntityModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class BeatCraftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerEntityRenderers();
        registerModelLayers();
    }

    private void registerEntityRenderers() {
        EntityRendererRegistry.register(BeatCraftEntities.COLOR_NOTE, ColorNoteRenderer::new);
    }

    private void registerModelLayers() {
        EntityModelLayerRegistry.registerModelLayer(ColorNoteEntityModel.MODEL_LAYER, ColorNoteEntityModel::getTextureModelData);
    }

}