package com.beatcraft.client.lightshow.event.handlers;

import com.beatcraft.common.data.types.Color;
import com.beatcraft.common.event.EventHandler;
import com.beatcraft.client.lightshow.event.events.LightEventV2;
import com.beatcraft.client.lightshow.lights.LightState;
import net.minecraft.util.Mth;

import java.util.List;

public class LightEventHandlerV2 extends EventHandler<LightState, LightEventV2> {
    public LightEventHandlerV2(List<LightEventV2> events) {
        super(events, new LightState(new Color(0, 0, 0, 0), 0));
    }

    @Override
    public void onEventInterrupted(LightEventV2 event, float normalTime) {

    }

    @Override
    public void onInsideEvent(LightEventV2 event, float normalTime) {
        var ls = event.getLightState();

        float fadeTime = 1 - (float)Math.pow(1 - normalTime, 3);

        if (event.isFlashType()) {
            float brightness = Mth.lerp(fadeTime, 1.2f, 1);
            var s = ls.copy();
            s.setBrightness(brightness);
            state = s;
        } else if (event.isFadeType()) {
            float brightness = Mth.lerp(fadeTime, 1.2f, 0);
            var s = ls.copy();
            s.setBrightness(brightness);
            state = s;
        } else if (event.isTransitionType()) {
            event.setFadeFrom(state);
            state = event.getFaded(normalTime);
        }

    }

    @Override
    public void onEventPassed(LightEventV2 event) {
        if (event.isFadeType()) {
            state = new LightState(new Color(0), 0);
        } else {
            state = event.getLightState();
        }
    }
}
