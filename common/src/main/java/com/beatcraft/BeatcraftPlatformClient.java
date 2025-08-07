package com.beatcraft;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class BeatcraftPlatformClient {
    @ExpectPlatform
    public static void initClient() {
        throw new AssertionError("Platform implementation is missing");
    }

}
