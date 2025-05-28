package com.beatcraft.render.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.object.BombNote;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.effect.MirrorHandler;
import com.beatcraft.render.instancing.BombNoteInstanceData;
import com.beatcraft.render.mesh.MeshLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PhysicalBombNote extends PhysicalGameplayObject<BombNote> {
    public static final ModelIdentifier bombNoteArrowModelID = new ModelIdentifier(Identifier.of(BeatCraft.MOD_ID,  "bomb_note"), "inventory");
    private static final int overlay = OverlayTexture.getUv(0, false);

    private static final Hitbox DOT_GOOD_CUT_BOUNDS = new Hitbox(
        new Vector3f(-0.4f, -0.4f, -0.75f),
        new Vector3f(0.4f, 0.4f, 0.25f)
    );

    private static final Hitbox BAD_CUT_BOUNDS = new Hitbox(
        new Vector3f(-0.175f, -0.175f, -0.175f),
        new Vector3f(0.175f, 0.175f, 0.175f)
    );

    public PhysicalBombNote(BombNote data) {
        super(data);
    }

    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {
        var localPos = matrices.peek();

        var renderPos = localPos.getPositionMatrix().getTranslation(MemoryPool.newVector3f());
        var renderRotation = localPos.getPositionMatrix().getUnnormalizedRotation(MemoryPool.newQuaternionf());
        var c = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        var flipped = new Matrix4f().scale(1, -1, 1);
        flipped.translate(0, c.y * 2f, 0);
        flipped.translate(renderPos);
        flipped.rotate(renderRotation);
        flipped.scale(0.5f);

        renderPos.add(c);

        MeshLoader.BOMB_NOTE_INSTANCED_MESH.draw(new BombNoteInstanceData(localPos.getPositionMatrix(), data.getColor(), GameLogicHandler.globalDissolve, data.getMapIndex()));
        MeshLoader.MIRROR_BOMB_NOTE_INSTANCED_MESH.draw(new BombNoteInstanceData(flipped, data.getColor(), GameLogicHandler.globalDissolve, data.getMapIndex()));

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
