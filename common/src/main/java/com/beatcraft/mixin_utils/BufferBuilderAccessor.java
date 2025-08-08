package com.beatcraft.mixin_utils;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

public interface BufferBuilderAccessor {
    ByteBufferBuilder beatcraft$getAllocator();
}