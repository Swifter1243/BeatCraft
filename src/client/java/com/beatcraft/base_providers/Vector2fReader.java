package com.beatcraft.base_providers;

import org.joml.Vector2f;

public record Vector2fReader(float[] values) implements ValueReader<Vector2f> {
    public Vector2f get(Vector2f dest) {
        return dest.set(values);
    }
    public Vector2f get() {
        return get(new Vector2f());
    }
}
