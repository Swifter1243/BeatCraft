package com.beatcraft.networking;

import com.beatcraft.Beatcraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

public record SetSaberModelPayload(String id, byte targets) implements CustomPacketPayload {

    public static final StreamCodec<FriendlyByteBuf, SetSaberModelPayload> CODEC = CustomPacketPayload.codec(
        SetSaberModelPayload::write,
        SetSaberModelPayload::read
    );

    public static final Type<SetSaberModelPayload> TYPE = new Type<>(Beatcraft.id("packets"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeByteArray(id.getBytes(Charset.defaultCharset()));
        buffer.writeByte(targets);
    }

    public static SetSaberModelPayload read(FriendlyByteBuf buffer) {
        var id = buffer.readByteArray();
        var targets = buffer.readByte();

        return new SetSaberModelPayload(new String(id), targets);
    }

}
