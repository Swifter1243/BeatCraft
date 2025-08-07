package com.beatcraft.client.animation.base_providers;

import org.joml.Vector4f;

public record Vector4fReader(float[] values) implements ValueReader<Vector4f> {
    public Vector4f get(Vector4f dest) {
        return dest.set(values);
    }

    @Override
    public Vector4f get() {
        return get(new Vector4f());
    }
}