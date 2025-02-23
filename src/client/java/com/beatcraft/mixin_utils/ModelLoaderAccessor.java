package com.beatcraft.mixin_utils;

import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;

import java.io.IOException;

public interface ModelLoaderAccessor {
    public JsonUnbakedModel beatCraft$loadJsonModel(Identifier id) throws IOException;
}
