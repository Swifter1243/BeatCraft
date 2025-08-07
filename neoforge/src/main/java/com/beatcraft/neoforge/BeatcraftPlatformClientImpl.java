package com.beatcraft.neoforge;

import com.beatcraft.client.BeatcraftClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BeatcraftPlatformClientImpl {

    @OnlyIn(Dist.CLIENT)
    public static void initClient() {
        BeatcraftClient.init();
    }
}
