package com.beatcraft;

import com.beatcraft.common.data.components.ModComponents;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: make sure far clipping plane is 5000m (matches beat saber)

public final class Beatcraft {
    public static final String MOD_ID = "beatcraft";

    public static Logger LOGGER = LoggerFactory.getLogger("Beatcraft");

    public static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(MOD_ID, path);
    }

    public static void init() {
        ModComponents.register();
    }
}
