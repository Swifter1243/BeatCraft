package com.beatcraft.render.effect;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.utils.MathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class SkyFogController {

    private static double last = 1.0;
    private static final double STEP = 0.01;

    private static double stepTo(double target) {
        if (last == target) return last;
        if (target > last) {
            last = Math.min(target, last+STEP);
        } else {
            last = Math.max(target, last-STEP);
        }

        return last;
    }

    // multiplicative modifier. 1.0 means sky color will be normal, 0.0 means it will be black
    public static double getColorModifier() {
        boolean playing = BeatmapPlayer.isPlaying();

        double radius = playing ? 300 : 100;
        double outer = playing ? 75 : 25;

        var player = MinecraftClient.getInstance().player;
        if (player == null) return stepTo(1.0);

        float dist = (float) player.getPos().distanceTo(new Vec3d(0, 0, 0));

        if (dist <= radius) {
            return stepTo(0.0);
        } else if (dist <= radius + outer) {
            double delta = MathUtil.inverseLerp(radius, radius+outer, dist);
            return stepTo(delta);
        }

        return stepTo(1.0);
    }


}
