package com.beatcraft.client.beatmap.object.physical;

import com.beatcraft.client.animation.AnimationState;
import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.beatmap.object.data.BombNote;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.common.memory.MemoryPool;
import com.beatcraft.client.render.instancing.BombNoteInstanceData;
import com.beatcraft.client.render.mesh.MeshLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class PhysicalBombNote extends PhysicalGameplayObject<BombNote> {

    private static final Hitbox DOT_GOOD_CUT_BOUNDS = new Hitbox(
        new Vector3f(-0.4f, -0.4f, -0.75f),
        new Vector3f(0.4f, 0.4f, 0.25f)
    );

    private static final Hitbox BAD_CUT_BOUNDS = new Hitbox(
        new Vector3f(-0.175f, -0.175f, -0.175f),
        new Vector3f(0.175f, 0.175f, 0.175f)
    );

    public PhysicalBombNote(BeatmapController map, BombNote data) {
        super(map, data);
    }

    @Override
    protected void objectRender(PoseStack matrices, Camera camera, AnimationState animationState, float alpha) {
        var localPos = matrices.last();

        var renderPos = localPos.pose().getTranslation(MemoryPool.newVector3f());
        var renderRotation = localPos.pose().getUnnormalizedRotation(MemoryPool.newQuaternionf());
        var renderScale = localPos.pose().getScale(MemoryPool.newVector3f());
        var c = camera.getPosition().toVector3f();

        var flipped = new Matrix4f().scale(1, -1, 1);
        flipped.translate(0, c.y * 2f, 0);
        flipped.translate(renderPos);
        flipped.rotate(renderRotation);
        flipped.scale(renderScale);

        MemoryPool.release(renderPos, renderScale);
        MemoryPool.release(renderRotation);

        var dissolve = Math.max(mapController.logic.globalDissolve, getBaseDissolve());
        MeshLoader.BOMB_NOTE_INSTANCED_MESH.draw(BombNoteInstanceData.create(localPos.pose(), data.getColor().copy().withAlpha(alpha), dissolve, data.getMapIndex()));
        MeshLoader.MIRROR_BOMB_NOTE_INSTANCED_MESH.draw(BombNoteInstanceData.create(flipped, data.getColor().copy().withAlpha(alpha), dissolve, data.getMapIndex()));

    }

    @Override
    public float getCollisionDistance() {
        return 0.607f;
    }

    @Override
    public Hitbox getBadCutBounds() {
        return BAD_CUT_BOUNDS;
    }

    @Override
    public Hitbox getGoodCutBounds() {
        return DOT_GOOD_CUT_BOUNDS;
    }
}
