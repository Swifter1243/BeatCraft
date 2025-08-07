package com.beatcraft.client.beatmap.object.data;

import org.joml.Quaternionf;
import org.joml.Random;

public class SpawnQuaternionPool {

    private static final Random random = new Random();
    private static final float ROTATION_RANGE = 0.6f;

    private static float getRandomComponent() {
        return (random.nextFloat() * ROTATION_RANGE * 2) - ROTATION_RANGE;
    }

    public static Quaternionf getRandomQuaternion() {
        float x = getRandomComponent();
        float y = getRandomComponent();
        float z = getRandomComponent();
        return new Quaternionf().rotateZYX(x, y, z);
    }
}
