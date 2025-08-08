package com.beatcraft.mixin_utils;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public interface ModelLoaderAccessor {
    BlockModel beatCraft$loadJsonModel(ResourceLocation id) throws IOException;
}