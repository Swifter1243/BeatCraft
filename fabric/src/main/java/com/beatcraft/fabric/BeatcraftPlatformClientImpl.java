package com.beatcraft.fabric;

import com.beatcraft.client.BeatcraftClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class BeatcraftPlatformClientImpl {
    @Environment(EnvType.CLIENT)
    public static void initClient() {
        BeatcraftClient.init();
    }
}
