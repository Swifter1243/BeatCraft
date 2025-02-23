package com.beatcraft.replay;

import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record PlayFrame(float beat, Vector3f leftSaberPosition, Quaternionf leftSaberRotation, Vector3f rightSaberPosition, Quaternionf rightSaberRotation) {
    public void write(JsonArray array) {
        JsonObject json = new JsonObject();
        json.addProperty("b", beat);
        json.add("lp", toJson(leftSaberPosition));
        json.add("lr", toJson(leftSaberRotation));
        json.add("rp", toJson(rightSaberPosition));
        json.add("rr", toJson(rightSaberRotation));
        array.add(json);
    }

    public static PlayFrame load(JsonObject json) {
        float b = json.get("b").getAsFloat();
        Vector3f lp = JsonUtil.getVector3(json.getAsJsonArray("lp"));
        Quaternionf lr = getQuaternion(json.getAsJsonArray("lr"));
        Vector3f rp = JsonUtil.getVector3(json.getAsJsonArray("rp"));
        Quaternionf rr = getQuaternion(json.getAsJsonArray("rr"));
        return new PlayFrame(b, lp, lr, rp, rr);
    }

    public static JsonElement toJson(Vector3f pos) {
        JsonArray json = new JsonArray();
        json.add(pos.x);
        json.add(pos.y);
        json.add(pos.z);
        return json;
    }

    public static JsonElement toJson(Quaternionf rot) {
        JsonArray json = new JsonArray();
        json.add(rot.x);
        json.add(rot.y);
        json.add(rot.z);
        json.add(rot.w);
        return json;
    }

    public static Quaternionf getQuaternion(JsonArray json) {
        return new Quaternionf(
            json.get(0).getAsFloat(),
            json.get(1).getAsFloat(),
            json.get(2).getAsFloat(),
            json.get(3).getAsFloat()
        );
    }

}
