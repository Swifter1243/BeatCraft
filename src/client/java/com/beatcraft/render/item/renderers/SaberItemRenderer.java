package com.beatcraft.render.item.renderers;

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

        int color = currentItemStack.getOrDefault(ModComponents.SABER_COLOR_COMPONENT, 0);

        super.renderCubesOfBone(
            poseStack, bone, buffer, packedLight, packedOverlay,
            (bone.getName().equals("colored"))
                ? color + 0xFF000000
                : 0xFFFFFFFF
        );

        return true;
    }
}
