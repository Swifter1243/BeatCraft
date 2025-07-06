package com.beatcraft.base_providers;

import org.joml.Vector3f;

public record Vector3fReader(float[] values) implements ValueReader<Vector3f> {
    public Vector3f get(Vector3f dest) {
        return dest.set(values);
    }
    @Override
    public Vector3f get() {
        return get(new Vector3f());
    }
}
