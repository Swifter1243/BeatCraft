package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.data.types.Color;
import com.beatcraft.event.EventHandler;
import com.beatcraft.lightshow.event.events.LightEvent;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.utils.MathUtil;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

public class LightEventHandler extends EventHandler<LightState, LightEvent> {
    public LightEventHandler(ArrayList<LightEvent> events) {
        super(events, new LightState(new Color(0, 0, 0, 0), 0));
    }

    @Override
    public void onEventInterrupted(LightEvent event, float normalTime) {

    }

    @Override
    public void onInsideEvent(LightEvent event, float normalTime) {
        var ls = event.getLightState();

        if (event.isFlashType()) {
            float brightness = MathHelper.lerp(normalTime, 1.2f, 1);
            var s = ls.copy();
            s.setBrightness(brightness);
            state = s;
        } else if (event.isFadeType()) {
            float brightness = MathHelper.lerp(normalTime, 1.2f, 0);
            var s = ls.copy();
            s.setBrightness(brightness);
            state = s;
        } else if (event.isTransitionType()) {
            event.setFadeFrom(state);
            state = event.getFaded(normalTime);
        }

    }

    @Override
    public void onEventPassed(LightEvent event) {
        if (event.isFadeType()) {
            state = new LightState(new Color(0), 0);
        } else {
            state = event.getLightState();
        }
    }
}
