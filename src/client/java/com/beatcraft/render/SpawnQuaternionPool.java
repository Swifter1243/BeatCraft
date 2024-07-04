package com.beatcraft.render;

import org.joml.Quaternionf;
import org.joml.Random;
import org.joml.Vector3f;

public class SpawnQuaternionPool {

    private static final Random random = new Random();
    private static final int ROTATION_RANGE = 60;

    private static float getRandomComponent() {
        return random.nextInt(ROTATION_RANGE * 2) - ROTATION_RANGE;
    }

    public static Quaternionf getRandomQuaternion() {
        float x = getRandomComponent();
        float y = getRandomComponent();
        float z = getRandomComponent();
        return new Quaternionf().rotateZYX(x, y, z);
    }
}
