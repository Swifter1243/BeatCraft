package com.beatcraft.mixin;

import com.beatcraft.BeatCraft;
import com.beatcraft.mixin_utils.ModelLoaderAccessor;
import com.beatcraft.render.mesh.MeshLoader;
import com.beatcraft.render.object.PhysicalBombNote;
import com.beatcraft.render.object.PhysicalChainNoteHead;
import com.beatcraft.render.object.PhysicalChainNoteLink;
import com.beatcraft.render.object.PhysicalColorNote;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Mixin(ModelLoader.class)
public abstract class CustomModelLoader implements ModelLoaderAccessor {
    @Shadow protected abstract void loadItemModel(ModelIdentifier modelId);

    @Shadow protected abstract JsonUnbakedModel loadModelFromJson(Identifier id) throws IOException;

    @Inject(method = "<init>", at = @At(value = "CONSTANT", args = "stringValue=special"))
    private void loadModels(
            BlockColors blockColors,
            Profiler profiler,
            Map<Identifier, JsonUnbakedModel> jsonUnbakedModels,
            Map<Identifier, List<BlockStatesLoader.SourceTrackedData>> blockStates,
            CallbackInfo info
    ) {
        this.loadItemModel(PhysicalColorNote.noteDotModelID);
        this.loadItemModel(PhysicalColorNote.noteArrowModelID);
        this.loadItemModel(PhysicalColorNote.colorNoteBlockModelID);
        this.loadItemModel(PhysicalBombNote.bombNoteArrowModelID);
        this.loadItemModel(PhysicalChainNoteHead.chainHeadModelID);
        this.loadItemModel(PhysicalChainNoteLink.chainLinkModelID);
        this.loadItemModel(PhysicalChainNoteLink.chainDotModelID);

        MeshLoader.loadGameplayMeshes(this);
    }

    @Override
    public JsonUnbakedModel beatCraft$loadJsonModel(Identifier id) throws IOException {
        return loadModelFromJson(id);
    }
}