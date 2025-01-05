package com.beatcraft.render.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.beatmap.data.object.BombNote;
import com.beatcraft.beatmap.data.object.ColorNote;
import com.beatcraft.utils.MathUtil;
import com.beatcraft.utils.NoteMath;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector2f;

public class PhysicalBombNote extends PhysicalGameplayObject<BombNote> {
    public static final ModelIdentifier bombNoteArrowModelID = new ModelIdentifier(Identifier.of(BeatCraft.MOD_ID,  "bomb_note"), "inventory");
    private static final int overlay = OverlayTexture.getUv(0, false);

    public PhysicalBombNote(BombNote data) {
        super(data);
    }

    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {
        var localPos = matrices.peek();

        BakedModel model = mc.getBakedModelManager().getModel(bombNoteArrowModelID);
        mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, model, getData().getColor().getRed(), getData().getColor().getGreen(), getData().getColor().getBlue(), 255, overlay);
    }
}
