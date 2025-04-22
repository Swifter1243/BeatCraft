package com.beatcraft.lightshow.event.events;

import com.beatcraft.beatmap.data.object.BeatmapObject;
import com.beatcraft.event.IEvent;

public class TransformEvent extends BeatmapObject implements IEvent {


    @Override
    public float getEventBeat() {
        return getBeat();
    }

    @Override
    public float getEventDuration() {
        return 0;
    }
}
