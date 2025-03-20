package com.beatcraft.render.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.object.BombNote;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.effect.MirrorHandler;
import com.beatcraft.render.mesh.MeshLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
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

        var renderPos = localPos.getPositionMatrix().getTranslation(new Vector3f()).add(MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f());
        var renderRotation = localPos.getPositionMatrix().getUnnormalizedRotation(new Quaternionf());
        BeatcraftRenderer.recordNoteRenderCall((tri, cam) -> {
            MeshLoader.BOMB_RENDER_MESH.color = data.getColor().toARGB();
            MeshLoader.BOMB_RENDER_MESH.drawToBuffer(tri, renderPos, renderRotation, cam);
        });
        MirrorHandler.recordMirrorNoteDraw((tri, cam) -> {
            MeshLoader.BOMB_RENDER_MESH.color = data.getColor().toARGB();
            MeshLoader.BOMB_RENDER_MESH.drawToBufferMirrored(tri, renderPos, renderRotation, cam);
        });

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
