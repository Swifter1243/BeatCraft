package com.beatcraft;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class BeatCraftPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        //System.loadLibrary("renderdoc"); // render debugging
    }
}
