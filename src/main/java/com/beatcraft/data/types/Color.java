package com.beatcraft.data.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Color {
    private float alpha = 0;
    private float red = 0;
    private float green = 0;
    private float blue = 0;

    public Color(float red, float green, float blue) {
        this.setRed(red);
        this.setGreen(green);
        this.setBlue(blue);
        this.setAlpha(1);
    }

    public Color(float red, float green, float blue, float alpha) {
        this.setRed(red);
        this.setGreen(green);
        this.setBlue(blue);
        this.setAlpha(alpha);
    }

    public Color(int hex) {
        int a = (hex >> 24) & 0xFF;
        int r = (hex >> 16) & 0xFF;
        int g = (hex >> 8) & 0xFF;
        int b = hex & 0xFF;
        float fr = r / 255.0f;
        float fg = g / 255.0f;
        float fb = b / 255.0f;
        float fa = a / 255.0f;
        this.setRed(fr);
        this.setGreen(fg);
        this.setBlue(fb);
        this.setAlpha(fa);
    }

    public Color() {}

    public static Color fromJsonObject(JsonObject json) {
        Color color = new Color();
        color.setRed(json.get("r").getAsFloat());
        color.setGreen(json.get("g").getAsFloat());
        color.setBlue(json.get("b").getAsFloat());
        color.setAlpha(1);
        return color;
    }

    public static Color fromJsonArray(JsonArray json) {
        Color color = new Color();
        color.setRed(json.get(0).getAsFloat());
        color.setGreen(json.get(1).getAsFloat());
        color.setBlue(json.get(2).getAsFloat());
        color.setAlpha(1);
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

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getAlpha() {
        return alpha;
    }

    public void set(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = 1;
    }

    public int toARGB() {
        return toARGB(alpha);
    }

    public int toARGB(float alpha) {
        int color = (int) (alpha * 255);
        color <<= 8;
        color += (int) (red * 255);
        color <<= 8;
        color += (int) (green * 255);
        color <<= 8;
        color += (int) (blue * 255);
        return color;
    }

}