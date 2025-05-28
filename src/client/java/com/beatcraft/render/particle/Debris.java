package com.beatcraft.render.particle;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.data.types.Color;
import com.beatcraft.debug.BeatCraftDebug;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.instancing.ColorNoteInstanceData;
import com.beatcraft.render.instancing.InstancedMesh;
import com.beatcraft.render.mesh.MeshLoader;
import com.beatcraft.render.mesh.TriangleMesh;
import com.beatcraft.utils.MathUtil;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Debris implements Particle {

    private final Vector3f position;
    private final Quaternionf orientation;
    private final Vector3f velocity;
    private final Vector3f decay;
    private final Quaternionf spin;
    public Vector4f slice;
    private final int randomIndex;
    private final Color color;
    private final double spawnTime;
    private final InstancedMesh<ColorNoteInstanceData> mesh;

    public boolean persistent = false;

    public Debris(Vector3f position, Quaternionf orientation, Vector3f velocity, Quaternionf spin, Vector4f slice, Color color, InstancedMesh<ColorNoteInstanceData> mesh) {
        this.position = position;
        this.velocity = velocity;
        this.orientation = orientation;
        this.spin = spin;
        this.slice = slice;
        this.color = color;
        this.decay = new Vector3f(0.99f, 0.99f, 0.99f);
        this.spawnTime = System.nanoTime() / 1_000_000_000d;
        this.mesh = mesh;
        this.randomIndex = Random.create().nextBetween(0, 200);
    }

    @Override
    public void update(float deltaTime, BufferBuilder buffer, Vector3f cameraPos) {
        if (!(persistent || !BeatmapPlayer.isPlaying())) {
            orientation.mul(spin.mul(deltaTime, new Quaternionf())).normalize();
            position.add(velocity.mul(deltaTime, new Vector3f()));
            velocity.add(new Vector3f(0, -9.81f, 0).mul(deltaTime));
            velocity.mul(decay);
        }

        var pos = new Matrix4f().identity();
        pos.translate(position);
        pos.translate(new Vector3f(cameraPos).negate());
        pos.rotate(orientation);

        pos.scale(0.5f);

        mesh.draw(new ColorNoteInstanceData(
            pos, color,
            (float) BeatCraftDebug.getValue("dissolve", 0f),
            randomIndex, slice
        ));
        // TODO: setup instance-based with cut plane

    }

    @Override
    public boolean shouldRemove() {
        return (!persistent) && MathUtil.inverseLerp(spawnTime, spawnTime+5f, System.nanoTime()/1_000_000_000d) >= 1;
    }
}
