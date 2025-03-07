package com.beatcraft.beatmap.data;

import com.beatcraft.BeatCraft;
import com.beatcraft.beatmap.data.object.BeatmapObject;
import com.beatcraft.data.types.Color;
import com.google.gson.JsonObject;


public class ChromaMaterial extends BeatmapObject {
    // currently shader does nothing, im not doing shaders for this everything will just be unlit :shrug:
    protected String shader;
    protected Color color;
    protected String name;

    public ChromaMaterial load(String name, JsonObject properties) {
        shader = properties.get("shader").toString();
        try{
        color = Color.fromJsonArray(properties.get("color").getAsJsonArray());
        }
        catch(NullPointerException e){
            BeatCraft.LOGGER.warn("Material has no color property, defaulting to white");
            color = new Color(255,255,255,1);
        }
        return this;
    }
    public String getName(){
        return name;
    }
    public String getShader() {
        return shader;
    }
    public Color getColor() {
        return color;
    }
}
