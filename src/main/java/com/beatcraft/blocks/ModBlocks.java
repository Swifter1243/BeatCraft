package com.beatcraft.blocks;

import com.beatcraft.BeatCraft;
import com.beatcraft.blocks.entity.*;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.concurrent.Callable;

public class ModBlocks {

    public static final BlackMirrorBlock BLACK_MIRROR_BLOCK = (BlackMirrorBlock) register(new BlackMirrorBlock(), "black_mirror_block");
    public static final ReflectiveMirrorBlock REFLECTIVE_MIRROR_BLOCK = (ReflectiveMirrorBlock) register(new ReflectiveMirrorBlock(), "reflective_mirror_block");
    public static final ReflectiveMirrorStripBlock REFLECTIVE_MIRROR_STRIP_BLOCK = (ReflectiveMirrorStripBlock) register(new ReflectiveMirrorStripBlock(), "reflective_mirror_strip_block");

    public static final FilledLightTileBlock FILLED_LIGHT_TILE_BLOCK = (FilledLightTileBlock) register(new FilledLightTileBlock(), "filled_light_tile");
    public static final EdgeLightTileBlock EDGE_LIGHT_TILE_BLOCK = (EdgeLightTileBlock) register(new EdgeLightTileBlock(), "edge_light_tile");
    public static final CornerLightTileBlock CORNER_LIGHT_TILE_BLOCK = (CornerLightTileBlock) register(new CornerLightTileBlock(), "corner_light_tile");
    public static final ColumnLightTileBlock COLUMN_LIGHT_TILE_BLOCK = (ColumnLightTileBlock) register(new ColumnLightTileBlock(), "column_light_tile");
    public static final EndLightTileBlock END_LIGHT_TILE_BLOCK = (EndLightTileBlock) register(new EndLightTileBlock(), "end_light_tile");


    // block entities
    public static final BlockEntityType<BlackMirrorBlockEntity> BLACK_MIRROR_BLOCK_ENTITY = registerBlockEntity(
        BLACK_MIRROR_BLOCK, BlackMirrorBlockEntity::new, "black_mirror_block_entity"
    );

    public static final BlockEntityType<ReflectiveMirrorBlockEntity> REFLECTIVE_MIRROR_BLOCK_ENTITY = registerBlockEntity(
        REFLECTIVE_MIRROR_BLOCK, ReflectiveMirrorBlockEntity::new, "reflective_mirror_block_entity"
    );

    public static final BlockEntityType<ReflectiveMirrorStripBlockEntity> REFLECTIVE_MIRROR_STRIP_BLOCK_ENTITY = registerBlockEntity(
        REFLECTIVE_MIRROR_STRIP_BLOCK, ReflectiveMirrorStripBlockEntity::new, "reflective_mirror_strip_block_entity"
    );

    public static final BlockEntityType<EdgeLightTileBlockEntity> EDGE_LIGHT_BLOCK_ENTITY_TYPE = registerBlockEntity(
        EDGE_LIGHT_TILE_BLOCK, EdgeLightTileBlockEntity::new, "edge_light_block_entity"
    );

    public static final BlockEntityType<CornerLightTileBlockEntity> CORNER_LIGHT_BLOCK_ENTITY_TYPE = registerBlockEntity(
        CORNER_LIGHT_TILE_BLOCK, CornerLightTileBlockEntity::new, "corner_light_block_entity"
    );

    public static final BlockEntityType<EndLightTileBlockEntity> END_LIGHT_BLOCK_ENTITY_TYPE = registerBlockEntity(
        END_LIGHT_TILE_BLOCK, EndLightTileBlockEntity::new, "end_light_block_entity"
    );

    public static final BlockEntityType<FilledLightTileBlockEntity> FILLED_LIGHT_BLOCK_ENTITY_TYPE = registerBlockEntity(
        FILLED_LIGHT_TILE_BLOCK, FilledLightTileBlockEntity::new, "filled_light_block_entity"
    );

    public static final BlockEntityType<ColumnLightTileBlockEntity> COLUMN_LIGHT_BLOCK_ENTITY_TYPE = registerBlockEntity(
        COLUMN_LIGHT_TILE_BLOCK, ColumnLightTileBlockEntity::new, "column_light_block_entity"
    );

    private static<T extends BlockEntity> BlockEntityType<T> registerBlockEntity(Block parent, BlockEntityType.BlockEntityFactory<T> factory, String id) {
        try {
            return Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(BeatCraft.MOD_ID, id),
                BlockEntityType.Builder.create(factory, parent).build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Block register(Block block, String path) {
        Registry.register(Registries.BLOCK, Identifier.of(BeatCraft.MOD_ID, path), block);
        Registry.register(Registries.ITEM, Identifier.of(BeatCraft.MOD_ID, path), new BlockItem(block, new Item.Settings()));
        return block;
    }

    public static void init() {

    }

}
