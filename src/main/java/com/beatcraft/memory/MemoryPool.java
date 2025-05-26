package com.beatcraft.memory;

import com.beatcraft.BeatCraft;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Stack;

public class MemoryPool {

    private static final Stack<Vector3f> sharedVector3fs = new Stack<>();
    private static int sharedVector3fBalance = 0;

    private static final Stack<Vector2f> sharedVector2fs = new Stack<>();
    private static int sharedVector2fBalance = 0;

    private static final Stack<Quaternionf> sharedQuaternionfs = new Stack<>();
    private static int sharedQuaternionfBalance = 0;

    public static Vector3f newVector3f(Vector3f copyFrom) {
        return newVector3f(copyFrom.x, copyFrom.y, copyFrom.z);
    }

    public static Vector3f newVector3f(Vec3d copyFrom) {
        return newVector3f((float) copyFrom.x, (float) copyFrom.y, (float) copyFrom.z);
    }

    public static Vector3f newVector3f() {
        return newVector3f(0, 0, 0);
    }

    public static Vector3f newVector3f(float x, float y, float z) {
        sharedVector3fBalance++;
        return sharedVector3fs.isEmpty() ? new Vector3f(x, y, z) : sharedVector3fs.pop().set(x, y, z);
    }

    public static Vector2f newVector2f(Vector2f copyFrom) {
        return newVector2f(copyFrom.x, copyFrom.y);
    }

    public static Vector2f newVector2f() {
        return newVector2f(0, 0);
    }

    public static Vector2f newVector2f(float x, float y) {
        sharedVector2fBalance++;
        return sharedVector2fs.isEmpty() ? new Vector2f(x, y) : sharedVector2fs.pop().set(x, y);
    }

    public static Quaternionf newQuaternionf(Quaternionf copyFrom) {
        return newQuaternionf(copyFrom.x, copyFrom.y, copyFrom.z, copyFrom.w);
    }

    public static Quaternionf newQuaternionf() {
        return newQuaternionf(0, 0, 0, 1);
    }

    public static Quaternionf newQuaternionf(float x, float y, float z, float w) {
        sharedQuaternionfBalance++;
        return sharedQuaternionfs.isEmpty() ? new Quaternionf(x, y, z, w) : sharedQuaternionfs.pop().set(x, y, z, w);
    }


    public static void releaseSafe(Vector3f... vectors) {
        for (var vec : vectors) {
            sharedVector3fs.push(vec);
            sharedVector3fBalance--;
            if (sharedVector3fBalance < 0) {
                sharedVector3fBalance = 0;
            }
        }
    }

    public static void release(Vector3f... vectors) {
        for (var vec : vectors) {
            sharedVector3fs.push(vec);
            sharedVector3fBalance--;
            if (sharedVector3fBalance < 0) {
                throw new RuntimeException("Vector3f memory balance went negative. this would cause a memory leak!");
            }
        }
    }

    public static void release(Vector2f vec) {
        sharedVector2fs.push(vec);
        sharedVector2fBalance--;
        if (sharedVector2fBalance < 0) {
            throw new RuntimeException("Vector2f memory balance went negative. this would cause a memory leak!");
        }
    }

    public static void release(Vector2f... vectors) {
        for (var vec : vectors) {
            release(vec);
        }
    }

    public static void release(Quaternionf quat) {
        sharedQuaternionfs.push(quat);
        sharedQuaternionfBalance--;
        if (sharedQuaternionfBalance < 0) {
            throw new RuntimeException("Quaternionf memory balance went negative. this would cause a memory leak!");
        }
    }

    public static void releaseSafe(Quaternionf... quaternions) {
        for (var vec : quaternions) {
            sharedQuaternionfs.push(vec);
            sharedQuaternionfBalance--;
            if (sharedQuaternionfBalance < 0) {
                sharedQuaternionfBalance = 0;
            }
        }
    }

    public static void clear() {
        sharedVector2fs.clear();
        sharedVector3fs.clear();
        sharedQuaternionfs.clear();

        BeatCraft.LOGGER.info("final shared memory balance: V3:{} V2:{} Q:{}", sharedVector3fBalance, sharedVector2fBalance, sharedQuaternionfBalance);

        sharedVector3fBalance = 0;
        sharedVector2fBalance = 0;
        sharedQuaternionfBalance = 0;
    }

}
