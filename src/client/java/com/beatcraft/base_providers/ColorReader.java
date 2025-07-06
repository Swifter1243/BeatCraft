package com.beatcraft.base_providers;

import com.beatcraft.data.types.Color;

public record ColorReader(float[] values) implements ValueReader<Color> {
    public Color get(Color dest) {
        dest.set(values[0], values[1], values[2], values[3]);
        return dest;
    }
    public Color get() {
        return get(new Color());
    }
}
