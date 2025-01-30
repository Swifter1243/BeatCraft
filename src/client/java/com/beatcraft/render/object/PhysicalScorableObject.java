package com.beatcraft.render.object;

import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.beatmap.data.object.ScorableObject;
import com.beatcraft.logic.GameLogicHandler;

public interface PhysicalScorableObject {
    ScorableObject score$getData();
    void score$setContactColor(NoteType type);
    void score$setCutResult(GameLogicHandler.CutResult cut);
    void score$cutNote();
    GameLogicHandler.CutResult score$getCutResult();
    int score$getMaxSwingInScore();
    int score$getMaxFollowThroughScore();
    int score$getMaxCutPositionScore();
    int score$getMaxSwingInAngle();
    int score$getMaxFollowThroughAngle();
}
