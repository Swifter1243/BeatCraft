package com.beatcraft.client.lightshow.environment;

import com.beatcraft.common.data.types.Color;

public class BoostableColor extends Color {

    public static Color leftColor = new Color();
    public static Color rightColor = new Color();
    public static Color whiteColor = new Color();

    private final int environmentColor;

    public BoostableColor(int environmentColor) {
        this.environmentColor = environmentColor;
    }

    @Override
    public BoostableColor copy() {
        return new BoostableColor(environmentColor);
    }

    @Override
    public float getRed() {
        return (switch (environmentColor) {
            case 1 -> rightColor;
            case 2 -> whiteColor;
            default -> leftColor;
        }).getRed();
    }

    @Override
    public float getGreen() {
        return (switch (environmentColor) {
            case 1 -> rightColor;
            case 2 -> whiteColor;
            default -> leftColor;
        }).getGreen();
    }

    @Override
    public float getBlue() {
        return (switch (environmentColor) {
            case 1 -> rightColor;
            case 2 -> whiteColor;
            default -> leftColor;
        }).getBlue();
    }

    @Override
    public float getAlpha() {
        return (switch (environmentColor) {
            case 1 -> rightColor;
            case 2 -> whiteColor;
            default -> leftColor;
        }).getAlpha();
    }
}
