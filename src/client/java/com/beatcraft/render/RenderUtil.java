package com.beatcraft.render;

import net.minecraft.util.math.Direction;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RenderUtil {
    public static Quaternionf getBlockRenderOrientation(Direction face) {
        Quaternionf q = new Quaternionf();

        switch (face) {
            case DOWN -> q.identity();
            case UP -> q.rotationX((float) Math.PI);
            case NORTH -> q.rotationX((float) Math.PI / 2);
            case SOUTH -> q.rotationX((float) -Math.PI / 2);
            case EAST -> q.rotationZ((float) Math.PI / 2);
            case WEST -> q.rotationZ((float) -Math.PI / 2);
        }
        return q;
    }

    public static Quaternionf getBlockRenderOrientation(Direction face, Direction rotation) {
        Quaternionf q = new Quaternionf();

        switch (face) {
            case DOWN -> q.identity();
            case UP -> q.rotationX((float) Math.PI);
            case NORTH -> q.rotationX((float) Math.PI / 2);
            case SOUTH -> q.rotationX((float) -Math.PI / 2);
            case EAST -> q.rotationZ((float) Math.PI / 2);
            case WEST -> q.rotationZ((float) -Math.PI / 2);
        }

        var local = new Vector3f(0, 0, -1);
        var target = rotation.getUnitVector().rotate(q.conjugate(new Quaternionf()));

        return q.mul(rotationBetween(local, target));
    }

    public static Quaternionf rotationBetween(Vector3f local, Vector3f target) {
        Vector3f axis = new Vector3f();
        local.cross(target, axis);
        float dot = local.dot(target);
        float angle = (float) Math.acos(Math.max(-1.0f, Math.min(1.0f, dot)));
        if (axis.lengthSquared() < 0.0001f) {
            if (dot > 0) {
                return new Quaternionf().identity();
            } else {
                axis = findPerpendicular(local).normalize();
                return new Quaternionf().rotationAxis((float) Math.PI, axis);
            }
        }

        return new Quaternionf().rotationAxis(angle, axis.normalize());
    }

    private static Vector3f findPerpendicular(Vector3f v) {
        return Math.abs(v.x) > Math.abs(v.z) ? new Vector3f(-v.y, v.x, 0) : new Vector3f(0, -v.z, v.y);
    }


}
