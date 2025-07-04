package com.beatcraft.networking.s2c;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

public record PlayerUntrackS2CPayload(UUID uuid) implements CustomPayload {
    public static CustomPayload.Id<PlayerUntrackS2CPayload> ID = new Id<>(BeatCraftNetworking.PLAYER_DISCONNECT_S2C);
    public static PacketCodec<PacketByteBuf, PlayerUntrackS2CPayload> CODEC = PacketCodec.of(PlayerUntrackS2CPayload::write, PlayerUntrackS2CPayload::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(uuid);
    }

    public static PlayerUntrackS2CPayload read(PacketByteBuf buf) {
        UUID uuid = buf.readUuid();
        return new PlayerUntrackS2CPayload(uuid);
    }

}
