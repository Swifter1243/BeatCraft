package com.beatcraft.menu;

import com.beatcraft.render.menu.TextInput;

public class KeyboardMenu extends Menu {

    public TextInput input;
    public boolean shift = false;

    public KeyboardMenu(TextInput input) {
        this.input = input;
    }

}
