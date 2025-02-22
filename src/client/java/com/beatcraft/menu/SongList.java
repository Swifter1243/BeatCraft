package com.beatcraft.menu;

import com.beatcraft.BeatCraft;
import com.beatcraft.data.menu.SongData;
import com.beatcraft.data.menu.SongDownloader;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SongList {

    private final ArrayList<SongData> songs = new ArrayList<>();


    public SongList() {

    }

    public List<SongData> getSongs() {
        return songs;
    }

    public List<SongData> getFiltered(String searchFilter) {
        ArrayList<SongData> list = new ArrayList<>();

        for (SongData data : songs) {
            if (data.getTitle().contains(searchFilter) || data.getSubtitle().contains(searchFilter) || data.getAuthor().contains(searchFilter)) {
                list.add(data);
            }
        }

        return list;
    }

    public SongData getById(String id) {
        for (SongData data : songs) {
            if (id.equals(data.getId())) {
                return data;
            }
        }
        return null;
    }

    /// loads beatmaps from `./beatmaps/`
    /// if the folder does not exist it will be created
    public void loadSongs() {
        String beatmapFolder = MinecraftClient.getInstance().runDirectory.toPath().toString() + "/beatmaps/";

        File folder = new File(beatmapFolder);

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                BeatCraft.LOGGER.error("Failed to create beatmaps folder");
                return;
            }
        }

        File[] subfolders = folder.listFiles(File::isDirectory);

        if (subfolders == null) {
            BeatCraft.LOGGER.error("Failed to load beatmaps");
            return;
        }

        songs.clear();
        for (File songFolder : subfolders) {
            try {
                SongData data = new SongData(songFolder.getAbsolutePath());
                songs.add(data);

                //SongDownloader.convertAllToPng(songFolder.getAbsolutePath()); // this will convert existing beatmaps to only contain png images instead of jpeg/jpg

            } catch (IOException e) {
                BeatCraft.LOGGER.error("Failed to load beatmap ", e);
            }
        }


    }

}
