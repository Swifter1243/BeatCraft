package com.beatcraft.common.utils;

import java.util.ArrayList;
import java.util.List;

public final class UnityRandom {
    private int x, y, z, w;

    public UnityRandom(int seed) {
        initState(seed);
    }

    private void initState(int seed) {
        x = seed;
        y = 362436069;
        z = 521288629;
        w = 88675123;

        for (int i = 0; i < 16; i++) {
            nextUInt();
        }
    }

    private int nextUInt() {
        int t = x ^ (x << 11);
        //noinspection SuspiciousNameCombination
        x = y;
        y = z;
        z = w;
        w = w ^ (w >>> 19) ^ t ^ (t >>> 8);
        return w;
    }

    public float value() {
        return (nextUInt() & 0xFFFFFF) / (float) 0x1000000;
    }

    public int range(int min, int max) {
        if (min >= max) return min;
        int r = nextUInt();
        int range = max - min;
        return min + (int) ((r & 0xFFFFFFFFL) % range);
    }

    public float range(float min, float max) {
        return min + (max - min) * value();
    }

    public<T> ArrayList<T> shuffle(List<T> list) {
        var src = new ArrayList<>(list);
        var out = new ArrayList<T>();
        for (int i = 0; i < list.size(); i++) {
            var next = range(0, src.size());
            out.add(src.remove(next));
        }
        return out;
    }

}

