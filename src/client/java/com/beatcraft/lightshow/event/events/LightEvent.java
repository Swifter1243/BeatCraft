package com.beatcraft.lightshow.event.events;

import com.beatcraft.beatmap.data.object.BeatmapObject;

public abstract class LightEvent extends BeatmapObject {

    public abstract boolean containsLightID(int lightID);

}
