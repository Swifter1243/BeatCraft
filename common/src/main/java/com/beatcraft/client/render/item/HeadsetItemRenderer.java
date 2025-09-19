package com.beatcraft.client.render.item;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.render.instancing.HeadsetInstanceData;
import com.beatcraft.client.render.mesh.MeshLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class HeadsetItemRenderer {

    public static boolean renderingInventoryEntity = false;

    public static final ModelResourceLocation FALLBACK_MODEL = new ModelResourceLocation(Beatcraft.id("headset"), "inventory");

    public void render(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {

        if (itemDisplayContext.equals(ItemDisplayContext.GUI) || itemDisplayContext.firstPerson()) {

            var model = Minecraft.getInstance().getModelManager().getModel(FALLBACK_MODEL);

            Minecraft.getInstance().getItemRenderer().render(
                stack, itemDisplayContext, false, poseStack, multiBufferSource,
                i, j, model
            );

            return;
        } else if (Minecraft.getInstance().screen != null && renderingInventoryEntity) {
            return;
        }

        var cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();

        var m = new Matrix4f().translate(cameraPos.x, cameraPos.y, cameraPos.z).mul(poseStack.last().pose());

        MeshLoader.HEADSET_INSTANCED_MESH.draw(HeadsetInstanceData.create(m));
        MeshLoader.HEADSET_INSTANCED_MESH.copyDrawToBloom();

    }
}
