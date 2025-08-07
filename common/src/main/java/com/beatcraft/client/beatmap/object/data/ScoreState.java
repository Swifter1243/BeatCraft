package com.beatcraft.client.beatmap.object.data;

public class ScoreState {

    private int type;
    private int score = 0;

    private ScoreState(int type) {
        this.type = type;
    }

    public static ScoreState unChecked() {
        return new ScoreState(0);
    }

    public static ScoreState badCut() {
        return new ScoreState(1);
    }

    public static ScoreState goodCut(int score) {
        var s = new ScoreState(2);
        s.score = score;
        return s;
    }

    public void setUnchecked() {
        this.score = 0;
        this.type = 0;
    }

    public void setBadCut() {
        this.score = 0;
        this.type = 1;
    }

    public void setGoodCut(int score) {
        this.score = score;
        this.type = 2;
    }

}
