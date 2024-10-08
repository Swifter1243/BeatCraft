package com.beatcraft.beatmap.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Color {
    private float red = 0;
    private float green = 0;
    private float blue = 0;

    public Color(float red, float green, float blue) {
        this.setRed(red);
        this.setGreen(green);
        this.setBlue(blue);
    }

    public Color() {}

    public static Color fromJsonObject(JsonObject json) {
        Color color = new Color();
        color.setRed(json.get("r").getAsFloat());
        color.setGreen(json.get("g").getAsFloat());
        color.setBlue(json.get("b").getAsFloat());
        return color;
    }

    public static Color fromJsonArray(JsonArray json) {
        Color color = new Color();
        color.setRed(json.get(0).getAsFloat());
        color.setGreen(json.get(1).getAsFloat());
        color.setBlue(json.get(2).getAsFloat());
        return color;
    }

    public float getRed() {
        return red;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public float getGreen() {
        return green;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public float getBlue() {
        return blue;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public void set(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
}