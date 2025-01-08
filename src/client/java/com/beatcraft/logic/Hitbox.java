package com.beatcraft.logic;

import org.joml.Vector3f;

public class Hitbox {

    private Vector3f min;
    private Vector3f max;

    public Hitbox(Vector3f min, Vector3f max) {
        this.min = min;
        this.max = max;
    }

    public boolean checkCollision(Vector3f pointA, Vector3f pointB) {
        Vector3f direction = new Vector3f(pointB).sub(pointA);
        Vector3f inverted = new Vector3f(1 / direction.x, 1 / direction.y, 1 / direction.z);

        float t1 = (min.x - pointA.x) * inverted.x;
        float t2 = (max.x - pointA.x) * inverted.x;
        float t3 = (min.y - pointA.y) * inverted.y;
        float t4 = (max.y - pointA.y) * inverted.y;
        float t5 = (min.z - pointA.z) * inverted.z;
        float t6 = (max.z - pointA.z) * inverted.z;

        float tMin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        float tMax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        return 0 <= tMax && tMin <= tMax && tMax <= 1.0f;

    }


}
