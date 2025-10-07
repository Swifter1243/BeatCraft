package com.beatcraft.client.menu;

import com.beatcraft.client.logic.Rank;
import com.beatcraft.client.render.HUDRenderer;

public class EndScreenData extends Menu {
    public int score;
    public Rank rank;
    public int maxCombo;
    public int goodCuts;
    public float accuracy;
    public int totalNotes;

    public EndScreenData(HUDRenderer hudRenderer, int score, Rank rank, int maxCombo, int goodCuts, float accuracy, int totalNotes) {
        super(hudRenderer);
        this.score = score;
        this.rank = rank;
        this.maxCombo = maxCombo;
        this.goodCuts = goodCuts;
        this.accuracy = accuracy;
        this.totalNotes = totalNotes;
    }

}
