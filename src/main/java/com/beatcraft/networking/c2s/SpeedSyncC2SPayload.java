package com.beatcraft.networking.c2s;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SpeedSyncC2SPayload(float speed) implements CustomPayload {
    public static Id<SpeedSyncC2SPayload> ID = new Id<>(BeatCraftNetworking.SPEED_SYNC_C2S);
    public static PacketCodec<PacketByteBuf, SpeedSyncC2SPayload> CODEC = PacketCodec.of(SpeedSyncC2SPayload::write, SpeedSyncC2SPayload::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeFloat(speed);
    }

    public static SpeedSyncC2SPayload read(PacketByteBuf buf) {
        return new SpeedSyncC2SPayload(buf.readFloat());
    }

}
