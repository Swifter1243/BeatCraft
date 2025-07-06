package com.beatcraft.networking.c2s;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SongPauseC2SPayload(boolean paused) implements CustomPayload {
    public static Id<SongPauseC2SPayload> ID = new Id<>(BeatCraftNetworking.SONG_PAUSE_C2S);
    public static PacketCodec<PacketByteBuf, SongPauseC2SPayload> CODEC = PacketCodec.of(SongPauseC2SPayload::write, SongPauseC2SPayload::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeBoolean(paused);
    }

    public static SongPauseC2SPayload read(PacketByteBuf buf) {
        return new SongPauseC2SPayload(buf.readBoolean());
    }
}
