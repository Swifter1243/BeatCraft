package com.beatcraft.client;

import com.beatcraft.common.data.PlayerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BeatcraftClient {

    public static PlayerConfig playerConfig;

    public static void init() {
        playerConfig = PlayerConfig.loadFromFile();
    }


}
