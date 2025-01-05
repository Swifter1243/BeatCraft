package com.beatcraft.data.components;

import com.beatcraft.BeatCraft;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModComponents {

    public static final ComponentType<Integer> SABER_COLOR_COMPONENT = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        Identifier.of(BeatCraft.MOD_ID, "saber_color"),
        ComponentType.<Integer>builder().codec(Codec.INT).build()
    );

    public static void init() {
        // must be called or the class won't load
    }

}
