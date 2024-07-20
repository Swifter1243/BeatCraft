package com.beatcraft.mixin.client;

import com.beatcraft.render.object.PhysicalBombNote;
import com.beatcraft.render.object.PhysicalColorNote;
import net.minecraft.client.color.block.BlockColors;
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

import java.util.List;
import java.util.Map;

@Mixin(ModelLoader.class)
public abstract class CustomModelLoader {
    @Shadow protected abstract void addModel(ModelIdentifier modelId);

    @Inject(method = "<init>", at = @At(value = "CONSTANT", args = "stringValue=special"))
    private void loadModels(
            BlockColors blockColors,
            Profiler profiler,
            Map<Identifier, JsonUnbakedModel> jsonUnbakedModels,
            Map<Identifier, List<ModelLoader.SourceTrackedData>> blockStates,
            CallbackInfo info) {
        this.addModel(PhysicalColorNote.noteDotModelID);
        this.addModel(PhysicalColorNote.noteArrowModelID);
        this.addModel(PhysicalColorNote.colorNoteBlockModelID);
        this.addModel(PhysicalBombNote.bombNoteArrowModelID);
    }
}