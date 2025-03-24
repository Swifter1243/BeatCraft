package com.beatcraft.mixin;

import com.beatcraft.render.BeatCraftRenderLayers;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Mixin(RenderLayer.class)
public class RenderLayerMixin {
    @ModifyExpressionValue(method = "<clinit>", at= @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;", remap = false))
    private static ImmutableList<RenderLayer> addBlockRenderLayer(ImmutableList<RenderLayer> prev) {
        RenderLayer[] newLayers = Arrays.copyOf(prev.toArray(RenderLayer[]::new), prev.size() + 1);
        newLayers[prev.size()] = BeatCraftRenderLayers.getBloomfogSolid();
        return ImmutableList.copyOf(newLayers);
    }
}
