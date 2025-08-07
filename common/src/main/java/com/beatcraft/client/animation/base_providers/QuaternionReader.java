package com.beatcraft.client.animation.base_providers;

import org.joml.Quaternionf;

public record QuaternionReader(float[] value) implements ValueReader<Quaternionf> {
    @Override
    public Quaternionf get(Quaternionf dest) {
        return dest.rotationYXZ(value[1], value[0], value[2]);
    }

    @Override
    public Quaternionf get() {
        return get(new Quaternionf());
    }
}