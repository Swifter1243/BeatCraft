package com.beatcraft.client.lightshow.event.events;

import com.beatcraft.client.beatmap.object.data.BeatmapObject;

public abstract class LightEvent extends BeatmapObject {

    public abstract boolean containsLightID(int lightID);

}
