package com.beatcraft.blocks.entity;

import com.beatcraft.BeatCraft;
import com.beatcraft.blocks.ModBlocks;
import com.beatcraft.data.types.Color;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.function.UnaryOperator;

public class ColorNoteDisplayBlockEntity extends BlockEntity {
    public ColorNoteDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.COLOR_NOTE_DISPLAY_BLOCK_ENTITY_TYPE, pos, state);
    }

    public static final ComponentType<Integer> CUT_ANGLE = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        BeatCraft.id("cut_angle"),
        ComponentType.<Integer>builder().codec(Codec.INT).build()
    );
    public static final ComponentType<Integer> ROTATION_ANGLE = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        BeatCraft.id("rotation_angle"),
        ComponentType.<Integer>builder().codec(Codec.INT).build()
    );
    public static final ComponentType<Integer> COLOR = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        BeatCraft.id("display_color"),
        ComponentType.<Integer>builder().codec(Codec.INT).build()
    );

    public static void init() {}

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, BeatCraft.id(id), builderOperator.apply(ComponentType.builder()).build());
    }

    public Integer cutAngle = 0;

    public Integer rotationAngle = 0;

    public Color color = new Color(0.75294f, 0.188f, 0.188f);

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        cutAngle = nbt.getInt("cut_angle");
        rotationAngle = nbt.getInt("rotation_angle");
        color = new Color(nbt.getInt("color"));
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("cut_angle", cutAngle);
        nbt.putInt("rotation_angle", rotationAngle);
        nbt.putInt("color", color.toARGB());
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        cutAngle = components.getOrDefault(CUT_ANGLE, 0);
        rotationAngle = components.getOrDefault(ROTATION_ANGLE, 0);
        color = new Color(components.getOrDefault(COLOR, 0xFFC03030));
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);
        componentMapBuilder.add(CUT_ANGLE, cutAngle);
        componentMapBuilder.add(ROTATION_ANGLE, rotationAngle);
        componentMapBuilder.add(COLOR, color.toARGB());
    }
}
