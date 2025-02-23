package com.beatcraft.networking.c2s;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record BeatSyncC2SPayload(float beat) implements CustomPayload {
    public static CustomPayload.Id<BeatSyncC2SPayload> ID = new Id<>(BeatCraftNetworking.BEAT_SYNC_C2S);
    public static PacketCodec<PacketByteBuf, BeatSyncC2SPayload> CODEC = PacketCodec.of(BeatSyncC2SPayload::write, BeatSyncC2SPayload::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeFloat(beat);
    }

    public static BeatSyncC2SPayload read(PacketByteBuf buf) {
        return new BeatSyncC2SPayload(buf.readFloat());
    }

}
