package com.beatcraft.beatmap.data;

import com.beatcraft.data.types.Color;

public class SaberSyncedColor extends Color {

    public static Color leftColor = new Color();
    public static Color rightColor = new Color();

    private final int saberColor;

    public SaberSyncedColor(int saberColor) {
        this.saberColor = saberColor;
    }

    @Override
    public SaberSyncedColor copy() {
        return new SaberSyncedColor(saberColor);
    }

    @Override
    public float getRed() {
        return (saberColor == 0 ? leftColor : rightColor).getRed();
    }

    @Override
    public float getGreen() {
        return (saberColor == 0 ? leftColor : rightColor).getGreen();
    }

    @Override
    public float getBlue() {
        return (saberColor == 0 ? leftColor : rightColor).getBlue();
    }

    @Override
    public float getAlpha() {
        return (saberColor == 0 ? leftColor : rightColor).getAlpha();
    }
}
