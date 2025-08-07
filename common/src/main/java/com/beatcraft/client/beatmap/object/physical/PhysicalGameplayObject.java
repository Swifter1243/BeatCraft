package com.beatcraft.client.beatmap.object.physical;

import com.beatcraft.client.beatmap.object.data.GameplayObject;
import com.beatcraft.client.beatmap.object.data.ScoreState;
import com.beatcraft.client.beatmap.object.data.SpawnQuaternionPool;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PhysicalGameplayObject<T extends GameplayObject> {
    private static final float JUMP_FAR_Z = 500;
    private static final float JUMP_SECONDS = 0.4f;
    protected static final float SIZE_SCALAR = 0.5f;
    protected static final Vector3f WORLD_OFFSET = new Vector3f(0, 0.8f, 1f);


    protected final Quaternionf spawnQuaternion = SpawnQuaternionPool.getRandomQuaternion();
    protected Quaternionf baseRotation = new Quaternionf();
    protected Quaternionf lookRotation = new Quaternionf();
    protected Vector3f position = new Vector3f();
    protected Quaternionf orientation = new Quaternionf();

    /// useful for rendering and collisions, constructed from position and orientation each frame
    private final Matrix4f localTransform = new Matrix4f();

    public final T data;

    protected boolean despawned = false;
    protected final ScoreState scoreState = ScoreState.unChecked();

    public PhysicalGameplayObject(T data) {
        this.data = data;
    }

}
