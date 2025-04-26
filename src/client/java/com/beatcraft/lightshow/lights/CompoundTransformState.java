package com.beatcraft.lightshow.lights;

import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CompoundTransformState {

    // Axis swizzle
    public enum Swizzle {
        XYZ,
        XZY,
        YXZ,
        YZX,
        ZXY,
        ZYX,
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
        NNN
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

    private float[] applySwizzle(float[] xyz, Swizzle swizzle) {
        return switch (swizzle) {
            case XYZ -> xyz;
            case XZY -> new float[]{xyz[0], xyz[2], xyz[1]};
            case YXZ -> new float[]{xyz[1], xyz[0], xyz[2]};
            case YZX -> new float[]{xyz[1], xyz[2], xyz[0]};
            case ZXY -> new float[]{xyz[2], xyz[0], xyz[1]};
            case ZYX -> new float[]{xyz[2], xyz[1], xyz[0]};
        };
    }

    private float[] applyPolarity(float[] xyz, Polarity polarity) {
        return switch (polarity) {
            case PPP -> xyz;
            case PPN -> new float[]{ xyz[0],  xyz[1], -xyz[2]};
            case PNP -> new float[]{ xyz[0], -xyz[1],  xyz[2]};
            case PNN -> new float[]{ xyz[0], -xyz[1], -xyz[2]};
            case NPP -> new float[]{-xyz[0],  xyz[1],  xyz[2]};
            case NPN -> new float[]{-xyz[0],  xyz[1], -xyz[2]};
            case NNP -> new float[]{-xyz[0], -xyz[1],  xyz[2]};
            case NNN -> new float[]{-xyz[0], -xyz[1], -xyz[2]};
        };
    }

    public Vector3f getTranslation(Swizzle axisSwizzle, Polarity polarity) {

        var xyz = applyPolarity(applySwizzle(new float[]{tx, ty, tz}, axisSwizzle), polarity);


        return new Vector3f(xyz);
    }

    public Quaternionf getOrientation(Swizzle axisSwizzle, Polarity polarity, TriFunction<Float, Float, Float, Quaternionf> quaternionBuilder) {
        var xyz = applyPolarity(applySwizzle(new float[]{rx, ry, rz}, axisSwizzle), polarity);

        float dx = xyz[0] * MathHelper.RADIANS_PER_DEGREE;
        float dy = xyz[1] * MathHelper.RADIANS_PER_DEGREE;
        float dz = xyz[2] * MathHelper.RADIANS_PER_DEGREE;

        return quaternionBuilder.apply(dx, dy, dz);
    }

}
