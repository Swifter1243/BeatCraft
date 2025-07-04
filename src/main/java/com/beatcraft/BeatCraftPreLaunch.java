package com.beatcraft;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class BeatCraftPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            //System.loadLibrary("renderdoc"); // render debugging
        }
    }
}
