package com.beatcraft.replay;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.render.menu.ModifierMenuPanel;

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

        try {
            Replayer.loadReplay(replayFilePath);
        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to run replay '{}'", mapID, e);
        }
    }
}
