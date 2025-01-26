package com.beatcraft.render;


import java.util.ArrayList;

public class HUDRenderer {

    private static boolean showHUD = true;
    private static boolean advancedHUD = true;

    private static class ScorePanel {

        private final int score;
        private double spawnTime;

        public ScorePanel(int score) {
            this.score = score;
            this.spawnTime = System.nanoTime() / 1_000_000_000d;
        }
    }

    private ArrayList<ScorePanel> scores = new ArrayList<>();


    public static void render() {



        if (!showHUD) return;


    }



}
