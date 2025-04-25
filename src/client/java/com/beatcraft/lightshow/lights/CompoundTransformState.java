package com.beatcraft.lightshow.lights;

import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CompoundTransformState {

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

    public Vector3f getTranslation() {
        return new Vector3f(tx, ty, tz);
    }

    public Quaternionf getOrientation() {
        return new Quaternionf().rotationXYZ(rx * MathHelper.RADIANS_PER_DEGREE, ry * MathHelper.RADIANS_PER_DEGREE, rz * MathHelper.RADIANS_PER_DEGREE);
    }

}
