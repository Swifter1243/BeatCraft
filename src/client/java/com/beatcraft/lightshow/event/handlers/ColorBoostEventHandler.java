package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.event.EventHandler;
import com.beatcraft.event.VoidEventHandler;
import com.beatcraft.lightshow.environment.BoostableColor;
import com.beatcraft.lightshow.event.events.ColorBoostEvent;

import java.util.List;

public class ColorBoostEventHandler extends VoidEventHandler<ColorBoostEvent> {
    public ColorBoostEventHandler(List<ColorBoostEvent> events) {
        super(events);
    }

    @Override
    public void onEventInterrupted(ColorBoostEvent event, float normalTime) {

    }

    @Override
    public void onInsideEvent(ColorBoostEvent event, float normalTime) {

    }

    @Override
    public void onEventPassed(ColorBoostEvent event) {
        if (BeatmapPlayer.currentBeatmap != null) {
            var cs = BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme();
            if (event.boosted) {
                BoostableColor.leftColor = cs.getEnvironmentLeftColorBoost();
                BoostableColor.rightColor = cs.getEnvironmentRightColorBoost();
                BoostableColor.whiteColor = cs.getEnvironmentWhiteColorBoost();
            } else {
                BoostableColor.leftColor = cs.getEnvironmentLeftColor();
                BoostableColor.rightColor = cs.getEnvironmentRightColor();
                BoostableColor.whiteColor = cs.getEnvironmentWhiteColor();
            }
        }
    }
}
