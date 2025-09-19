package com.beatcraft.mixin;

import com.beatcraft.client.render.item.HeadsetItemRenderer;
import com.beatcraft.client.render.mesh.MeshLoader;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

    @Shadow
    protected abstract void loadSpecialItemModelAndDependencies(ModelResourceLocation modelResourceLocation);

    @Inject(
        method = "<init>",
        at = @At(value = "CONSTANT", args = "stringValue=special")
    )
    private void loadModels(
        BlockColors blockColors, ProfilerFiller profilerFiller,
        Map<ResourceLocation, BlockModel> models,
        Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> states,
        CallbackInfo info
    ) {
        this.loadSpecialItemModelAndDependencies(HeadsetItemRenderer.FALLBACK_MODEL);
    }

}
