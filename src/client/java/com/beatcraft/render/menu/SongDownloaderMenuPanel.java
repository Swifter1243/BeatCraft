package com.beatcraft.render.menu;

import com.beatcraft.menu.SongDownloaderMenu;

public class SongDownloaderMenuPanel extends MenuPanel<SongDownloaderMenu> {

    public SongDownloaderMenuPanel() {
        super(new SongDownloaderMenu());
        backgroundColor = 0;
        position.set(0.1f, 2f, 6);
        size.set(1000, 500);
    }
}
