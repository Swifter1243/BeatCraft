package com.beatcraft.render.menu;

import com.beatcraft.menu.SettingsMenu;

public class SettingsMenuPanel extends MenuPanel<SettingsMenu> {
    public SettingsMenuPanel() {
        super(new SettingsMenu());
        backgroundColor = 0;
        position.set(0.1f, 2f, 6);
        size.set(1000, 500);
    }
}
