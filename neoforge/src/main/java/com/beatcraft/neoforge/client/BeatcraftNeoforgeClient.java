package com.beatcraft.neoforge.client;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = Beatcraft.MOD_ID, dist = Dist.CLIENT)
public class BeatcraftNeoforgeClient {
    public BeatcraftNeoforgeClient() {
        BeatcraftClient.init();
    }
}
