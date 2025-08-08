package com.beatcraft.client.render;

import net.minecraft.client.renderer.MultiBufferSource;

public class HUDRenderer {

    public enum MenuScene {
        InGame,
        SongSelect,
        MainMenu,
        Settings,
        Downloader,
        EndScreen,
        ConfirmSongDelete,
        Paused,
        SaberPreview,
    }

    public static MultiBufferSource buffers;
    public static final int TEXT_LIGHT = 255;


}
