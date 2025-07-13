package com.beatcraft.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ErrorMessageMenu extends Menu {
    public ArrayList<String> lines = new ArrayList<>();

    public void setContent(String message) {
        if (message == null) {
            lines = new ArrayList<>(List.of("Unknown Error"));
        } else {
            lines = new ArrayList<>(Arrays.stream(message.split("\n")).toList());
        }
    }

    public String getLine(int l) {
        if (l < lines.size()) {
            return lines.get(l);
        }
        return "";
    }

    public void close() {
        lines.clear();
    }

    public boolean shouldDisplay() {
        return !lines.isEmpty();
    }

}
