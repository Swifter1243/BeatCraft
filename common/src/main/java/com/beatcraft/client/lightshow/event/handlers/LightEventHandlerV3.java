package com.beatcraft.client.lightshow.event.handlers;

import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.lightshow.event.events.LightEventV3;
import com.beatcraft.client.lightshow.lights.LightState;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.common.event.EventHandler;
import net.minecraft.util.Mth;

import java.util.List;

public class LightEventHandlerV3 extends EventHandler<LightState, LightEventV3> {


    public LightEventHandlerV3(List<LightEventV3> events) {
        super(events, new LightState(new Color(0, 0, 0, 0), 0));
    }

    public void addEvents(List<LightEventV3> events) {
        this.events.addAll(events);
        this.events.sort(Difficulty::compareObjects);
        reset();
    }

    public void clear() {
        this.events.clear();
    }

    @Override
    public void onEventInterrupted(LightEventV3 event, float normalTime) {

    }

    private float calcOscillation(float startBeat, float currentBeat, float frequency, boolean fade) {
        float beatsSinceStart = currentBeat - startBeat;

        float totalCycles = beatsSinceStart * frequency;
        float cyclePosition = totalCycles % 1.0f;

        if (fade) {
            if (cyclePosition < 0.5f) {
                return cyclePosition * 2.0f;
            } else {
                return 2.0f * (1.0f - cyclePosition);
            }
        } else {
            return cyclePosition >= 0.5f ? 1.0f : 0.0f;
        }
    }

    @Override
    public void onInsideEvent(LightEventV3 event, float normalTime) {
        var currentBeat = Mth.lerp(normalTime, event.getEventBeat(), event.getEventBeat()+event.getEventDuration());

        float easedTime = event.easing.apply(normalTime);

        event.startState.lerpFromTo(event.lightState, easedTime, state);

        var currentFrequency = Mth.lerp(easedTime, event.startState.strobeFrequency, event.lightState.strobeFrequency);

        var brightnessMod = (currentFrequency == 0) ? 1.0f : calcOscillation(event.getEventBeat(), currentBeat, currentFrequency, event.startState.strobeFade);

        var low = Mth.lerp(easedTime, event.startState.strobeBrightness, event.lightState.strobeBrightness);


        var currentBrightness = Mth.lerp(brightnessMod, low, state.getBrightness());

        state.setBrightness(currentBrightness);

    }

    @Override
    public void onEventPassed(LightEventV3 event) {
        state.set(event.lightState);
    }
}
