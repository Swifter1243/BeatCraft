package com.beatcraft.client.render.menu;

import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;

public class TextInput {

    public StringBuilder buffer = new StringBuilder();

    public TextInput() {

    }

    public void focus() {

    }

    public void unfocus() {

    }

    public void processInput(String value) {
        if (value.equals("backspace") || value.equals("delete")) {
            if (buffer.isEmpty()) return;
            buffer.deleteCharAt(buffer.length()-1);
        } else {
            buffer.append(value);
        }
    }


}
