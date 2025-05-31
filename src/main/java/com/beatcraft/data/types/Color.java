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

    public Color(Color other) {
        this.red = other.red;
        this.green = other.green;
        this.blue = other.blue;
        this.alpha = other.alpha;
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

    public Color copy() {
        return new Color(red, green, blue, alpha);
    }

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
        if (json.size() == 4)
            color.setAlpha(json.get(3).getAsFloat());
        else
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

    public Color withAlpha(float alpha) {
        this.setAlpha(Math.clamp(alpha, 0, 1));
        return this;
    }

    public void set(Color other) {
        red = other.getRed();
        green = other.getGreen();
        blue = other.getBlue();
        alpha = other.getAlpha();
    }
    public void set(float red, float green, float blue) {
        set(red, green, blue, 1);
    }
    public void set(float red, float green, float blue, float alpha) {
        this.setRed(red);
        this.setGreen(green);
        this.setBlue(blue);
        this.setAlpha(alpha);
    }

    public int toARGB() {
        return toARGB(getAlpha());
    }

    public int toARGB(float alpha) {
        int color = (int) (alpha * 255);
        color <<= 8;
        color += (int) (getRed() * 255);
        color <<= 8;
        color += (int) (getGreen() * 255);
        color <<= 8;
        color += (int) (getBlue() * 255);
        return color;
    }

    public int lerpBrightness(float brightness) {
        float newRed;
        float newGreen;
        float newBlue;
        float newAlpha;

        float r = getRed();
        float g = getGreen();
        float b = getBlue();
        float a = getAlpha();

        if (brightness <= 1.0f) {
            newRed = r * brightness;
            newGreen = g * brightness;
            newBlue = b * brightness;
            newAlpha = a * brightness;
        } else {
            float overBright = brightness - 1.0f;
            newRed = r + (1.0f - r) * overBright;
            newGreen = g + (1.0f - g) * overBright;
            newBlue = b + (1.0f - b) * overBright;
            newAlpha = a + (1 - a) * overBright;
        }

        newRed = Math.clamp(newRed, 0, 1);
        newGreen = Math.clamp(newGreen, 0, 1);
        newBlue = Math.clamp(newBlue, 0, 1);
        newAlpha = Math.clamp(newAlpha, 0, 1);

        int intRed = (int) (newRed * 255);
        int intGreen = (int) (newGreen * 255);
        int intBlue = (int) (newBlue * 255);
        int intAlpha = (int) (newAlpha * 255);

        return (intAlpha << 24) | (intRed << 16) | (intGreen << 8) | intBlue;
    }

    @Override
    public String toString() {
        return "Color{" +
            ", red=" + red +
            ", green=" + green +
            ", blue=" + blue +
            "alpha=" + alpha +
            '}';
    }
}