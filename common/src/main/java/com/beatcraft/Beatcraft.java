package com.beatcraft;

import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Beatcraft {
    public static final String MOD_ID = "beatcraft";

    public static Logger LOGGER = LoggerFactory.getLogger("Beatcraft");

    public static void init() {

        if (Platform.getEnv() == EnvType.CLIENT) {
            BeatcraftPlatformClient.initClient();
        }
    }
}
