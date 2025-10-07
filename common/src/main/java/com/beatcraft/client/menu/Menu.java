package com.beatcraft.client.menu;

import com.beatcraft.client.render.HUDRenderer;

public abstract class Menu {
    public final HUDRenderer hudRenderer;

    public Menu(HUDRenderer hudRenderer) {
        this.hudRenderer = hudRenderer;
    }
}
