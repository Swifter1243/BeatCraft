package com.beatcraft.client.replay;

import com.beatcraft.Beatcraft;

import java.io.IOException;

public record ReplayInfo(
    String mapID,
    String name,
    String set,
    String diff,
    String replayFilePath,
    boolean isMapPresent
) {

    public void play() {
        if (mapID == null) return;

        // try {
        //     Replayer.loadReplay(replayFilePath);
        // } catch (IOException e) {
        //     Beatcraft.LOGGER.error("Failed to run replay '{}'", mapID, e);
        // }
    }
}