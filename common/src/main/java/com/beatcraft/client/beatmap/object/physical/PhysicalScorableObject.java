package com.beatcraft.client.beatmap.object.physical;

import com.beatcraft.client.beatmap.data.CutDirection;
import com.beatcraft.client.beatmap.data.NoteType;
import com.beatcraft.client.beatmap.object.data.ScorableObject;
import com.beatcraft.client.beatmap.object.data.ScoreState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface PhysicalScorableObject {
    ScorableObject score$getData();
    void score$setContactColor(NoteType type);
    void score$setScoreState(ScoreState state);
    void score$cutNote();
    ScoreState score$getScoreState();
    int score$getMaxSwingInScore();
    int score$getMaxFollowThroughScore();
    int score$getMaxCutPositionScore();
    int score$getMaxSwingInAngle();
    int score$getMaxFollowThroughAngle();
    Quaternionf score$getLaneRotation();
    CutDirection score$getCutDirection();
    void score$spawnDebris(Vector3f point, Vector3f normal);
}
