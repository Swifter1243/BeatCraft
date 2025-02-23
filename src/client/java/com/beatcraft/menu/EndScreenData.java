package com.beatcraft.menu;

import com.beatcraft.logic.Rank;

public class EndScreenData extends Menu {
    public int score;
    public Rank rank;
    public int maxCombo;
    public int goodCuts;
    public float accuracy;
    public int totalNotes;

    public EndScreenData(int score, Rank rank, int maxCombo, int goodCuts, float accuracy, int totalNotes) {
        this.score = score;
        this.rank = rank;
        this.maxCombo = maxCombo;
        this.goodCuts = goodCuts;
        this.accuracy = accuracy;
        this.totalNotes = totalNotes;
    }

}
