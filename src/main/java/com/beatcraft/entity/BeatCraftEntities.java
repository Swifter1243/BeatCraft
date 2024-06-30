package com.beatcraft.entity;

import com.beatcraft.BeatCraft;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BeatCraftEntities {
    public static final EntityType<ColorNoteEntity> COLOR_NOTE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(BeatCraft.MOD_ID, "color_note"),
            EntityType.Builder.create(ColorNoteEntity::new, SpawnGroup.MISC).build(BeatCraft.MOD_ID)
    );
}
