package com.beatcraft.neoforge;

import com.beatcraft.Beatcraft;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Beatcraft.MOD_ID)
public final class BeatcraftNeoForge {
    public BeatcraftNeoForge() {
        // Run our common setup.
        Beatcraft.init();

    }
}
