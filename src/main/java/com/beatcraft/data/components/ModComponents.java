package com.beatcraft.data.components;

import com.beatcraft.BeatCraft;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.UnaryOperator;

public class ModComponents {

    public static final ComponentType<Integer> SABER_COLOR_COMPONENT = register("saber_color", builder -> builder.codec(Codec.INT));

    public static final ComponentType<Integer> AUTO_SYNC_COLOR = register("sync_color", builder -> builder.codec(Codec.INT));

    private static <T>ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(
            Registries.DATA_COMPONENT_TYPE, Identifier.of(BeatCraft.MOD_ID, name),
            builderOperator.apply(ComponentType.builder()).build()
        );
    }

    public static void init() {
        // must be called or the class won't load
    }

}
