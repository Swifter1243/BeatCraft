package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.BeatCraft;
import com.beatcraft.data.types.Color;
import com.beatcraft.event.EventHandler;
import com.beatcraft.lightshow.event.events.LightEventV3;
import com.beatcraft.lightshow.lights.LightState;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class LightEventHandlerV3 extends EventHandler<LightState, LightEventV3> {


    public LightEventHandlerV3(List<LightEventV3> events) {
        super(events, new LightState(new Color(0, 0, 0, 0), 0));
    }

    public void addEvents(List<LightEventV3> events) {
        this.events.addAll(events);
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
            return cyclePosition < 0.5f ? 1.0f : 0.0f;
        }
    }

    @Override
    public void onInsideEvent(LightEventV3 event, float normalTime) {
        var currentState = event.startState.lerpFromTo(event.lightState, normalTime);

        var currentBeat = MathHelper.lerp(normalTime, event.getEventBeat(), event.getEventBeat()+event.getEventDuration());
        var currentFrequency = MathHelper.lerp(normalTime, event.strobeStartFrequency, event.strobeFrequency);

        var brightnessMod = currentFrequency == 0 ? 1.0f : calcOscillation(event.getEventBeat(), currentBeat, currentFrequency, event.strobeFade);

        var low = MathHelper.lerp(normalTime, event.strobeStartBrightness, event.strobeBrightness);

        var currentBrightness = MathHelper.lerp(brightnessMod, low, currentState.getBrightness());

        currentState.setBrightness(currentBrightness);
        state = currentState;

    }

    @Override
    public void onEventPassed(LightEventV3 event) {
        state = event.lightState;
    }
}
