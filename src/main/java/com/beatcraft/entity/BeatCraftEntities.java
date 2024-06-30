package com.beatcraft.entity;

import com.beatcraft.BeatCraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BeatCraftEntities {
    public static final EntityType<ColorNoteEntity> COLOR_NOTE = register(
            "color_note", EntityType.Builder.create(ColorNoteEntity::new, SpawnGroup.MISC).setDimensions(0.6f, 0.6f).disableSaving());

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> type) {
        Identifier identifier = new Identifier(BeatCraft.MOD_ID, name);
        return Registry.register(Registries.ENTITY_TYPE, identifier, type.build());
    }
}
