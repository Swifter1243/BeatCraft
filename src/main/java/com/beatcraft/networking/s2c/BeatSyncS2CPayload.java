package com.beatcraft.networking.s2c;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record BeatSyncS2CPayload(float beat) implements CustomPayload {
    public static Id<BeatSyncS2CPayload> ID = new Id<>(BeatCraftNetworking.BEAT_SYNC_S2C);
    public static PacketCodec<PacketByteBuf, BeatSyncS2CPayload> CODEC = PacketCodec.of(BeatSyncS2CPayload::write, BeatSyncS2CPayload::read);
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
    
    public void write(PacketByteBuf buf) {
        buf.writeFloat(beat);
    }
    
    public static BeatSyncS2CPayload read(PacketByteBuf buf) {
        float beat = buf.readFloat();

        return new BeatSyncS2CPayload(beat);
    }
    
}
