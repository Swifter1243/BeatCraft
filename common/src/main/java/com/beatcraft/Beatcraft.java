package com.beatcraft;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Beatcraft {
    public static final String MOD_ID = "beatcraft";

    public static Logger LOGGER = LoggerFactory.getLogger("Beatcraft");

    public static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(MOD_ID, path);
    }

    public static void init() {

    }
}
