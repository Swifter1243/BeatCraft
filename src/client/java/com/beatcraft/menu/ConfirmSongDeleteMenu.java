package com.beatcraft.menu;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import com.beatcraft.data.menu.SongData;
import com.beatcraft.render.HUDRenderer;
import org.spongepowered.asm.util.Files;

import java.io.File;
import java.io.IOException;

public class ConfirmSongDeleteMenu extends Menu {
    public SongData songData;

    public ConfirmSongDeleteMenu(SongData data) {
        songData = data;
    }

    public void deleteSong() {

        try {
            Files.deleteRecursively(new File(songData.getSongFolder().toAbsolutePath().toString()));
        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to delete song folder '{}'", songData.getSongFolder(), e);
        }

        BeatCraftClient.songs.loadSongs();
        HUDRenderer.songSelectMenuPanel.initLayout();
        HUDRenderer.scene = HUDRenderer.MenuScene.SongSelect;

    }

}
