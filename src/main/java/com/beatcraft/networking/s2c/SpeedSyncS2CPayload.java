package com.beatcraft.networking.s2c;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SpeedSyncS2CPayload(float speed) implements CustomPayload {
    public static Id<SpeedSyncS2CPayload> ID = new Id<>(BeatCraftNetworking.SPEED_SYNC_S2C);
    public static PacketCodec<PacketByteBuf, SpeedSyncS2CPayload> CODEC = PacketCodec.of(SpeedSyncS2CPayload::write, SpeedSyncS2CPayload::read);
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
    
    public void write(PacketByteBuf buf) {
        buf.writeFloat(speed);
    }
    
    public static SpeedSyncS2CPayload read(PacketByteBuf buf) {
        float beat = buf.readFloat();

        return new SpeedSyncS2CPayload(beat);
    }
    
}
