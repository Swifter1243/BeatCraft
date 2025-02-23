package com.beatcraft.blocks;

import net.minecraft.block.Block;
import net.minecraft.sound.BlockSoundGroup;

// Material properties: blocks light glow. Visually Reflective
public class BlackMirrorBlock  extends Block {
    public BlackMirrorBlock() {
        super(Settings.create().hardness(3f).resistance(5f).sounds(BlockSoundGroup.GLASS));
    }
}
