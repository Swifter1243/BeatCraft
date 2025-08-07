package com.beatcraft.client.beatmap.object.data;

import com.beatcraft.client.beatmap.data.Difficulty;
import com.google.gson.JsonObject;

public interface IBeatmapData<T> {
    T loadV2(JsonObject json, Difficulty difficulty);
    T loadV3(JsonObject json, Difficulty difficulty);
}
