package com.beatcraft.beatmap.data;

public class ColorScheme {
    public Color noteLeftColor = new Color(0.75294f, 0.188f, 0.188f);
    public Color noteRightColor = new Color(0.1254f, 0.3921f, 0.6588f);
    public Color obstacleColor = new Color(1, 0.1882f, 0.1882f);
    public Color environmentLeftColor = new Color(0.7529f, 0.188f, 0.188f);
    public Color environmentLeftColorBoost = new Color(0.7529f, 0.188f, 0.188f);
    public Color environmentRightColor = new Color(0.18823f, 0.5960f, 1);
    public Color environmentRightColorBoost = new Color(0.18823f, 0.5960f, 1);
    public Color environmentWhiteColor = new Color(1, 1, 1);
    public Color environmentWhiteColorBoost = new Color(1, 1, 1);

    public static ColorScheme getEnvironmentColorScheme(String environment) {
        return switch (environment) {
            // add color schemes here sometime or something
            default -> new ColorScheme();
        };
    }
}
