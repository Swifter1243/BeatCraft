package com.beatcraft.mixin;


import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin implements BufferBuilderAccessor {

    @Shadow @Final private ByteBufferBuilder buffer;

    @Unique
    public ByteBufferBuilder beatcraft$getAllocator() {
        return this.buffer;
    }

}