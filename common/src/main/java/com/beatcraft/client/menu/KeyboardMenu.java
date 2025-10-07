package com.beatcraft.client.menu;

import com.beatcraft.client.render.HUDRenderer;
import com.beatcraft.client.render.menu.TextInput;

public class KeyboardMenu extends Menu {

    public TextInput input;
    public boolean shift = false;

    public KeyboardMenu(HUDRenderer hudRenderer, TextInput input) {
        super(hudRenderer);
        this.input = input;
    }

}
