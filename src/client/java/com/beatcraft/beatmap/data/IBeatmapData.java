package com.beatcraft.beatmap.data;

import com.beatcraft.beatmap.Difficulty;
import com.google.gson.JsonObject;

public interface IBeatmapData<T> {
    T loadV2(JsonObject json, Difficulty difficulty);

    T loadV3(JsonObject json, Difficulty difficulty);
}
