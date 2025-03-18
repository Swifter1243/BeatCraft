package com.beatcraft.render.effect;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.data.types.Color;
import com.beatcraft.lightshow.environment.Environment;
import com.beatcraft.utils.MathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SkyFogController {

    private static double last = 1.0;
    private static final double STEP = 0.01;

    private static void stepTo(double target) {
        if (last == target) return;
        if (target > last) {
            last = Math.min(target, last+STEP);
        } else {
            last = Math.max(target, last-STEP);
        }
    }

    // multiplicative modifier. 1.0 means sky color will be normal, 0.0 means it will be black
    public static double getColorModifier() {
        return last;
    }

    public static void updateColor() {
        boolean playing = BeatmapPlayer.isPlaying();

        double radius = playing ? 250 : 18;
        double outer = playing ? 75 : 6;

        var player = MinecraftClient.getInstance().player;
        if (player == null) {
            stepTo(1.0);
            return;
        }

        float dist = (float) player.getPos().distanceTo(new Vec3d(0, 0, 0));

        if (dist <= radius) {
            stepTo(0.0);
            return;
        } else if (dist <= radius + outer) {
            double delta = MathUtil.inverseLerp(radius, radius+outer, dist);
            stepTo(delta);
            return;
        }

        stepTo(1.0);
    }

    public static Color getGradientColor(Color original) {

        Color c2 = Environment.DEFAULT_FOG_COLOR;
        var beatmap = BeatmapPlayer.currentBeatmap;
        if (beatmap != null) {
            var ls = beatmap.lightShowEnvironment;
            if (ls != null) {
                c2 = ls.getFogColor();
            }
        }
        return MathUtil.lerpColor(c2, original, (float) getColorModifier());
    }

    private static final float sq2 =  3 * (float) (Math.sqrt(2f)/2f);
    private static final float dst = 3;
    private static final Vector3f[] offsets = new Vector3f[]{
        new Vector3f(-dst, 0, 0),
        new Vector3f(-sq2, 0, -sq2),
        new Vector3f(0, 0, -dst),
        new Vector3f(sq2, 0, -sq2),
        new Vector3f(dst, 0, 0),
        new Vector3f(sq2, 0, sq2),
        new Vector3f(0, 0, dst),
        new Vector3f(-sq2, 0, sq2),
    };

    private static Vector3f processVertex(Vector3f vIn, Quaternionf cameraRotation) {
        return vIn.rotate(cameraRotation, new Vector3f());
    }

    private static final Color blank = new Color(0);
    public static void render(BufferBuilder buffer, Vector3f cameraPos, Quaternionf cameraRot) {

        var color = getGradientColor(blank).toARGB();
        var color2 = getGradientColor(blank).lerpBrightness(0.5f);

        if (((color >> 24) & 0xFF) == 0) {
            return;
        }

        var a = offsets[7];

        for (int i = 0; i < offsets.length; i++) {
            var la = processVertex(a, cameraRot);
            var la2 = processVertex(a.add(0, 0.2f, 0, new Vector3f()), cameraRot);
            var o = offsets[i];
            var lo = processVertex(o, cameraRot);
            var lo2 = processVertex(o.add(0, 0.2f, 0, new Vector3f()), cameraRot);
            a = o;

            var n = la.sub(lo, new Vector3f());
            var n2 = la2.sub(lo2, new Vector3f());

            buffer.vertex(la).normal(n.x, n.y, n.z).color(color);
            buffer.vertex(lo).normal(-n.x, -n.y, -n.z).color(color);


            buffer.vertex(la2).normal(n2.x, n2.y, n2.z).color(color2);
            buffer.vertex(lo2).normal(-n2.x, -n2.y, -n2.z).color(color2);
        }

    }

}
