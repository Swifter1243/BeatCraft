package com.beatcraft.lightshow.environment;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.data.types.Color;

public class BoostableColor extends Color {

    public static Color leftColor;
    public static Color rightColor;
    public static Color whiteColor;

    private final int environmentColor;
    private Difficulty difficulty;

    public BoostableColor(int environmentColor, Difficulty difficulty) {
        this.environmentColor = environmentColor;
        this.difficulty = difficulty;
        var cs = difficulty.getSetDifficulty().getColorScheme();
        if (environmentColor == 0) {
            this.set(cs.getEnvironmentLeftColor());
        } else if (environmentColor == 1) {
            this.set(cs.getEnvironmentRightColor());
        } else if (environmentColor == 2) {
            this.set(cs.getEnvironmentWhiteColor());
        }
    }
}
