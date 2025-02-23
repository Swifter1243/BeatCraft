package com.beatcraft.render.item.renderers;

import com.beatcraft.BeatCraftClient;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.data.components.ModComponents;
import com.beatcraft.items.SaberItem;
import com.beatcraft.render.item.models.SaberItemModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.specialty.DynamicGeoItemRenderer;

public class SaberItemRenderer extends DynamicGeoItemRenderer<SaberItem> {
    public SaberItemRenderer() {
        super(new SaberItemModel());
    }

    @Override
    protected boolean boneRenderOverride(MatrixStack poseStack, GeoBone bone, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, int colour) {

        int color;

        int sync = currentItemStack.getOrDefault(ModComponents.AUTO_SYNC_COLOR, -1);

        if (sync == -1 || BeatmapPlayer.currentBeatmap == null) {
            color = currentItemStack.getOrDefault(ModComponents.SABER_COLOR_COMPONENT, 0) + 0xFF000000;
        } else if (sync == 0) {
            color = BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getNoteLeftColor().toARGB();
        } else {
            color = BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getNoteRightColor().toARGB();
        }


        super.renderCubesOfBone(
            poseStack, bone, buffer, 255, packedOverlay,
            (bone.getName().equals("colored"))
                ? color
                : 0xFFFFFFFF
        );

        return true;
    }
}
