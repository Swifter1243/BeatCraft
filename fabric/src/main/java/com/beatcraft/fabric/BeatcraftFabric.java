package com.beatcraft.fabric;

import com.beatcraft.Beatcraft;
import com.beatcraft.fabric.common.items.FabricItems;
import net.fabricmc.api.ModInitializer;

public final class BeatcraftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Beatcraft.init();

        FabricItems.init();

    }
}
