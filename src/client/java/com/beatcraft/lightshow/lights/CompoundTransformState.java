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
        switch (state.getAxis()) {
            case TX -> {
                tx = state.getValue();
            }
            case TY -> {
                ty = state.getValue();
            }
            case TZ -> {
                tz = state.getValue();
            }
            case RX -> {
                rx = state.getValue();
            }
            case RY -> {
                ry = state.getValue();
            }
            case RZ -> {
                rz = state.getValue();
            }
        }
    }

    public Vector3f getTranslation() {
        return new Vector3f(tx, ty, tz);
    }

    public Quaternionf getOrientation() {
        return new Quaternionf().rotationYXZ(ry * MathHelper.RADIANS_PER_DEGREE, rx * MathHelper.RADIANS_PER_DEGREE, rz * MathHelper.RADIANS_PER_DEGREE);
    }

}
