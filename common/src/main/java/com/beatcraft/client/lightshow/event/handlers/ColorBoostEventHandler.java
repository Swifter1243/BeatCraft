package com.beatcraft.client.lightshow.event.handlers;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.common.event.VoidEventHandler;
import com.beatcraft.client.lightshow.environment.BoostableColor;
import com.beatcraft.client.lightshow.event.events.ColorBoostEvent;

import java.util.List;

public class ColorBoostEventHandler extends VoidEventHandler<ColorBoostEvent> {
    BeatmapController mapController;

    public ColorBoostEventHandler(BeatmapController map, List<ColorBoostEvent> events) {
        super(events);
        mapController = map;
    }

    @Override
    public void onEventInterrupted(ColorBoostEvent event, float normalTime) {

    }

    @Override
    public void onInsideEvent(ColorBoostEvent event, float normalTime) {

    }

    @Override
    public void onEventPassed(ColorBoostEvent event) {
        var cs = mapController.difficulty.getSetDifficulty().getColorScheme();
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
