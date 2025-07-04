package com.beatcraft.networking.s2c;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record MapSyncS2CPayload(UUID player, String uid, String set, String diff, List<String> modifiers) implements CustomPayload {
    public static CustomPayload.Id<MapSyncS2CPayload> ID = new Id<>(BeatCraftNetworking.MAP_SYNC_S2C);
    public static PacketCodec<PacketByteBuf, MapSyncS2CPayload> CODEC = PacketCodec.of(MapSyncS2CPayload::write, MapSyncS2CPayload::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(player);
        buf.writeString(uid);
        buf.writeString(set);
        buf.writeString(diff);
        buf.writeInt(modifiers.size());
        for (var mod : modifiers) {
            buf.writeString(mod);
        }
    }

    public static MapSyncS2CPayload read(PacketByteBuf buf) {
        UUID player = buf.readUuid();
        String uid = buf.readString();
        String set = buf.readString();
        String diff = buf.readString();
        var numMods = buf.readInt();
        var mods = new ArrayList<String>();
        for (int i = 0; i < numMods; i++) {
            mods.add(buf.readString());
        }
        return new MapSyncS2CPayload(player, uid, set, diff, mods);
    }

}
