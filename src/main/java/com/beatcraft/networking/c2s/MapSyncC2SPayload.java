package com.beatcraft.networking.c2s;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

public record MapSyncC2SPayload(String uid, String set, String diff, List<String> modifiers) implements CustomPayload {
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
        buf.writeInt(modifiers.size());
        for (var mod : modifiers) {
            buf.writeString(mod);
        }
    }

    public static MapSyncC2SPayload read(PacketByteBuf buf) {
        String uid = buf.readString();
        String set = buf.readString();
        String diff = buf.readString();
        var numMods = buf.readInt();
        var mods = new ArrayList<String>();
        for (int i = 0; i < numMods; i++) {
            mods.add(buf.readString());
        }
        return new MapSyncC2SPayload(uid, set, diff, mods);
    }

}
