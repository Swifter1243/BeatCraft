package com.beatcraft.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class FabricPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
//        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
//            System.loadLibrary("renderdoc");
//        }
    }
}
