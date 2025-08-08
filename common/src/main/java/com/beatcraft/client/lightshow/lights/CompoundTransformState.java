package com.beatcraft.client.lightshow.lights;

import com.beatcraft.client.debug.BeatcraftDebug;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.function.TriFunction;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Function;

public class CompoundTransformState {

    // Axis swizzle
    public enum Swizzle {
        XYZ,
        XZY,
        YXZ,
        YZX,
        ZXY,
        ZYX,
        Dynamic;

        private Function<float[], float[]> remap;

        public static Swizzle getDynamic(String key) {
            return getDynamic(xyz -> {
                var sw = (int) BeatcraftDebug.getValue(key, 0);
                var swizzle = Swizzle.values()[Math.clamp(sw, 0, 5)];
                return CompoundTransformState.applySwizzle(xyz, swizzle);
            });
        }

        public static Swizzle getDynamic(Function<float[], float[]> remap) {
            var sw = Swizzle.Dynamic;
            sw.remap = remap;
            return sw;
        }

        Swizzle() {

        }
    }

    // Positive/Negative swizzle
    public enum Polarity {
        PPP,
        PPN,
        PNP,
        PNN,
        NPP,
        NPN,
        NNP,
        NNN,
        Dynamic;

        private Function<float[], float[]> remap;

        public static Polarity getDynamic(String key) {
            return getDynamic(xyz -> {
                var po = (int) BeatcraftDebug.getValue(key, 0);
                var polarity = Polarity.values()[Math.clamp(po, 0, 7)];
                return CompoundTransformState.applyPolarity(xyz, polarity);
            });
        }

        public static Polarity getDynamic(Function<float[], float[]> remap) {

            var po = Polarity.Dynamic;
            po.remap = remap;
            return po;
        }
    }

    private float tx = 0;
    private float ty = 0;
    private float tz = 0;

    private float rx = 0;
    private float ry = 0;
    private float rz = 0;

    public void updateState(TransformState state) {
        switch (state.axis) {
            case TX -> {
                tx = state.value;
            }
            case TY -> {
                ty = state.value;
            }
            case TZ -> {
                tz = state.value;
            }
            case RX -> {
                rx = state.value;
            }
            case RY -> {
                ry = state.value;
            }
            case RZ -> {
                rz = state.value;
            }
        }
    }

    public void reset() {
        tx = 0;
        ty = 0;
        tz = 0;
        rx = 0;
        ry = 0;
        rz = 0;
    }

    public static float[] applySwizzle(float[] xyz, Swizzle swizzle) {
        return switch (swizzle) {
            case XYZ -> xyz;
            case XZY -> new float[]{xyz[0], xyz[2], xyz[1]};
            case YXZ -> new float[]{xyz[1], xyz[0], xyz[2]};
            case YZX -> new float[]{xyz[1], xyz[2], xyz[0]};
            case ZXY -> new float[]{xyz[2], xyz[0], xyz[1]};
            case ZYX -> new float[]{xyz[2], xyz[1], xyz[0]};
            case Dynamic -> swizzle.remap.apply(xyz);
        };
    }

    public static float[] applyPolarity(float[] xyz, Polarity polarity) {
        return switch (polarity) {
            case PPP -> xyz;
            case PPN -> new float[]{ xyz[0],  xyz[1], -xyz[2]};
            case PNP -> new float[]{ xyz[0], -xyz[1],  xyz[2]};
            case PNN -> new float[]{ xyz[0], -xyz[1], -xyz[2]};
            case NPP -> new float[]{-xyz[0],  xyz[1],  xyz[2]};
            case NPN -> new float[]{-xyz[0],  xyz[1], -xyz[2]};
            case NNP -> new float[]{-xyz[0], -xyz[1],  xyz[2]};
            case NNN -> new float[]{-xyz[0], -xyz[1], -xyz[2]};
            case Dynamic -> polarity.remap.apply(xyz);
        };
    }

    public void getTranslation(Swizzle axisSwizzle, Polarity polarity, Vector3f dest) {

        var xyz = applyPolarity(applySwizzle(new float[]{tx, ty, tz}, axisSwizzle), polarity);

        dest.set(xyz);
    }

    public Quaternionf getOrientation(Swizzle axisSwizzle, Polarity polarity, TriFunction<Float, Float, Float, Quaternionf> quaternionBuilder) {
        var xyz = applyPolarity(applySwizzle(new float[]{rx, ry, rz}, axisSwizzle), polarity);

        float dx = xyz[0] * Mth.DEG_TO_RAD;
        float dy = xyz[1] * Mth.DEG_TO_RAD;
        float dz = xyz[2] * Mth.DEG_TO_RAD;

        return quaternionBuilder.apply(dx, dy, dz);
    }

}
