package com.beatcraft.render.menu;

import com.beatcraft.menu.ConfirmSongDeleteMenu;

public class ConfirmSongDeleteMenuPanel extends MenuPanel<ConfirmSongDeleteMenu> {
    public ConfirmSongDeleteMenuPanel(ConfirmSongDeleteMenu data) {
        super(data);
        backgroundColor = 0;
        position.set(0, 2f, 6);
        size.set(800, 500);
    }
}
