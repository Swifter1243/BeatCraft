package com.beatcraft.mixin;

import com.beatcraft.mixin_utils.BufferBuilderAccessible;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.BufferAllocator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin implements BufferBuilderAccessible {

    @Shadow @Final private BufferAllocator allocator;

    @Unique
    public BufferAllocator beatcraft$getAllocator() {
        return this.allocator;
    }

}
