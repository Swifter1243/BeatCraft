package com.beatcraft.client.menu;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.render.HUDRenderer;
import com.beatcraft.common.data.map.SongData;
import org.spongepowered.asm.util.Files;

import java.io.File;
import java.io.IOException;

public class ConfirmSongDeleteMenu extends Menu {
    public SongData songData;

    public ConfirmSongDeleteMenu(HUDRenderer hudRenderer, SongData data) {
        super(hudRenderer);
        songData = data;
    }

    public void deleteSong() {

        try {
            Files.deleteRecursively(new File(songData.getSongFolder().toAbsolutePath().toString()));
        } catch (IOException e) {
            Beatcraft.LOGGER.error("Failed to delete song folder '{}'", songData.getSongFolder(), e);
        }

        BeatcraftClient.songs.loadSongs();
        hudRenderer.songSelectMenuPanel.initLayout();
        hudRenderer.controller.scene = HUDRenderer.MenuScene.SongSelect;

    }

}
