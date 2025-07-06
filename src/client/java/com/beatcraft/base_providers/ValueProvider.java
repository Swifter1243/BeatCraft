package com.beatcraft.base_providers;

import com.beatcraft.data.types.Color;
import com.beatcraft.memory.MemoryPool;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.function.Consumer;

public interface ValueProvider {
    float[] getValues();
    default int getSize() {
        return getValues().length;
    }
}

interface RotationProvider {
    Quaternionf getRotation();
}

record StaticValueProvider(float[] values) implements ValueProvider {

    @Override
    public float[] getValues() {
        return values;
    }
}

record BaseProvider(float[] values) implements ValueProvider {

    @Override
    public float[] getValues() {
        return values;
    }
}

abstract class UpdatableValue implements ValueProvider {
    public abstract void update();
}

class QuaternionProvider extends UpdatableValue implements RotationProvider {

    private final float[] source;
    private final float[] values;
    private final Quaternionf rotation;

    QuaternionProvider(float[] source) {
        this.source = source;
        values = new float[3];
        rotation = new Quaternionf();
    }

    @Override
    public Quaternionf getRotation() {
        return rotation;
    }

    @Override
    public void update() {
        rotation.set(source[0], source[1], source[2], source[3]);
        Vector3f e = MemoryPool.newVector3f();
        rotation.getEulerAnglesXYZ(e);
        values[0] = e.x;
        values[1] = e.y;
        values[2] = e.z;
        MemoryPool.releaseSafe(e);
    }

    @Override
    public float[] getValues() {
        return values;
    }
}

class SwizzleProvider extends UpdatableValue {

    private final float[] source;
    private final int[] parts;
    private final float[] values;

    SwizzleProvider(float[] source, int[] parts) {
        this.source = source;
        this.parts = parts;
        values = new float[parts.length];
    }

    @Override
    public void update() {
        for (int i = 0; i < parts.length; i++) {
            values[i] = source[parts[i]];
        }
    }

    @Override
    public float[] getValues() {
        return values;
    }
}

class SmoothRotationProvider extends UpdatableValue {

    private final RotationProvider rotationProvider;
    private final float mult;
    private final float[] values = new float[3];
    private double lastTime;

    private final Quaternionf lastQuaternion = new Quaternionf();

    SmoothRotationProvider(RotationProvider provider, float mult) {
        rotationProvider = provider;
        this.mult = mult;
        lastTime = System.nanoTime() / 1_000_000_000d;
    }

    @Override
    public void update() {
        var t = System.nanoTime() / 1_000_000_000d;
        var dt = t - lastTime;
        lastTime = t;

        lastQuaternion.slerp(rotationProvider.getRotation(), (float) dt * mult);
        var e = MemoryPool.newVector3f();
        lastQuaternion.getEulerAnglesXYZ(e);
        values[0] = e.x;
        values[1] = e.y;
        values[2] = e.z;
        MemoryPool.releaseSafe(e);
    }

    @Override
    public float[] getValues() {
        return values;
    }
}

class SmoothValueProvider extends UpdatableValue {

    private final float[] source;
    private final float mult;
    private final float[] values;
    private double lastTime;

    SmoothValueProvider(float[] source, float mult) {
        this.source = source;
        this.mult = mult;
        this.values = new float[source.length];
        lastTime = System.nanoTime() / 1_000_000_000d;
    }

    @Override
    public void update() {
        var t = System.nanoTime() / 1_000_000_000d;
        var dt = t - lastTime;
        lastTime = t;

        for (int i = 0; i < source.length; i++) {
            values[i] = MathHelper.lerp((float) dt * mult, values[i], source[i]);
        }

    }

    @Override
    public float[] getValues() {
        return values;
    }
}


class BaseValueProvider extends UpdatableValue {

    private final float[] values;
    private final Consumer<float[]> updater;

    public BaseValueProvider(int size, Consumer<float[]> valueUpdater) {
        values = new float[size];
        updater = valueUpdater;
        update();
    }

    @Override
    public void update() {
        updater.accept(values);
    }

    @Override
    public float[] getValues() {
        return values;
    }
}

class BaseRotationProvider extends UpdatableValue implements RotationProvider {

    private final float[] values;
    private final Consumer<Quaternionf> updater;
    private final Quaternionf rotation = new Quaternionf();

    public BaseRotationProvider(Consumer<Quaternionf> valueUpdater) {
        values = new float[3];
        updater = valueUpdater;
    }

    @Override
    public void update() {
        updater.accept(rotation);
        var e = MemoryPool.newVector3f();
        rotation.getEulerAnglesXYZ(e);
        values[0] = e.x;
        values[1] = e.y;
        values[2] = e.z;
        MemoryPool.releaseSafe(e);
    }

    @Override
    public float[] getValues() {
        return values;
    }

    @Override
    public Quaternionf getRotation() {
        return rotation;
    }
}

class ValueMixer extends UpdatableValue {

    private final float[][] sources;
    private final float[] values;

    public ValueMixer(float[][] sources) {
        this.sources = sources;
        int i = 0;
        for (var src : sources) {
            if (src == null) continue;
            for (var ignored : src) {
                i++;
            }
        }
        this.values = new float[i];
    }

    @Override
    public void update() {
        int i = 0;
        for (var src : sources) {
            if (src == null) continue;
            for (var s : src) {
                values[i] = s;
                i++;
            }
        }
    }

    @Override
    public float[] getValues() {
        return values;
    }
}

class ValueOperator extends UpdatableValue {

    private final float[] src0;
    private final float[] src1;
    private final float[] values;
    private final String operation;

    public ValueOperator(float[] left, float[] right, String op) {
        src0 = left;
        src1 = right;
        values = new float[left.length];
        operation = op;
    }

    @Override
    public void update() {
        switch (operation) {
            case "opAdd" -> {
                for (int i = 0; i < src0.length; i++) {
                    values[i] = src0[i] + src1[i];
                }
            }
            case "opSub" -> {
                for (int i = 0; i < src0.length; i++) {
                    values[i] = src0[i] - src1[i];
                }
            }
            case "opMul" -> {
                for (int i = 0; i < src0.length; i++) {
                    values[i] = src0[i] * src1[i];
                }
            }
            case "opDiv" -> {
                for (int i = 0; i < src0.length; i++) {
                    values[i] = src0[i] / src1[i];
                }
            }
            default -> throw new IllegalArgumentException("Invalid operation: '" + operation + "'");
        }
    }

    @Override
    public float[] getValues() {
        return values;
    }
}


