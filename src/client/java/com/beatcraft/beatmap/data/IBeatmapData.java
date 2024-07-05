package com.beatcraft.beatmap.data;

import com.google.gson.JsonObject;

public interface IBeatmapData<T> {
    T loadV2(JsonObject json, Info.SetDifficulty setDifficulty);

    T loadV3(JsonObject json, Info.SetDifficulty setDifficulty);

    T loadV4(JsonObject objectJson, JsonObject lutJson, Info.SetDifficulty setDifficulty);
}
