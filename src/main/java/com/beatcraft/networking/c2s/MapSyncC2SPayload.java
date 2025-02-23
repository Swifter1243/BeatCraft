package com.beatcraft.networking.c2s;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record MapSyncC2SPayload(String uid, String set, String diff) implements CustomPayload {
    public static CustomPayload.Id<MapSyncC2SPayload> ID = new Id<>(BeatCraftNetworking.MAP_SYNC_C2S);
    public static PacketCodec<PacketByteBuf, MapSyncC2SPayload> CODEC = PacketCodec.of(MapSyncC2SPayload::write, MapSyncC2SPayload::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(uid);
        buf.writeString(set);
        buf.writeString(diff);
    }

    public static MapSyncC2SPayload read(PacketByteBuf buf) {
        String uid = buf.readString();
        String set = buf.readString();
        String diff = buf.readString();
        return new MapSyncC2SPayload(uid, set, diff);
    }

}
