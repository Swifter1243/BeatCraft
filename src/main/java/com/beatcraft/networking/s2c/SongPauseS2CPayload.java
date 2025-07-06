package com.beatcraft.networking.s2c;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SongPauseS2CPayload(boolean paused) implements CustomPayload {
    public static CustomPayload.Id<SongPauseS2CPayload> ID = new CustomPayload.Id<>(BeatCraftNetworking.SONG_PAUSE_S2C);
    public static PacketCodec<PacketByteBuf, SongPauseS2CPayload> CODEC = PacketCodec.of(SongPauseS2CPayload::write, SongPauseS2CPayload::read);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeBoolean(paused);
    }

    public static SongPauseS2CPayload read(PacketByteBuf buf) {
        return new SongPauseS2CPayload(buf.readBoolean());
    }
}
