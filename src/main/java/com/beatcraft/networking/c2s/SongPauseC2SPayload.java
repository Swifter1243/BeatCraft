package com.beatcraft.networking.c2s;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SongPauseC2SPayload() implements CustomPayload {
    public static Id<SongPauseC2SPayload> ID = new Id<>(BeatCraftNetworking.SONG_PAUSE_C2S);
    public static PacketCodec<PacketByteBuf, SongPauseC2SPayload> CODEC = PacketCodec.of(SongPauseC2SPayload::write, SongPauseC2SPayload::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {

    }

    public static SongPauseC2SPayload read(PacketByteBuf buf) {
        return new SongPauseC2SPayload();
    }
}
