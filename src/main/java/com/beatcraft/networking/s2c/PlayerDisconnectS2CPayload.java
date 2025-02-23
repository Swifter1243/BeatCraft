package com.beatcraft.networking.s2c;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

public record PlayerDisconnectS2CPayload(UUID uuid) implements CustomPayload {
    public static CustomPayload.Id<PlayerDisconnectS2CPayload> ID = new Id<>(BeatCraftNetworking.PLAYER_DISCONNECT_S2C);
    public static PacketCodec<PacketByteBuf, PlayerDisconnectS2CPayload> CODEC = PacketCodec.of(PlayerDisconnectS2CPayload::write, PlayerDisconnectS2CPayload::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(uuid);
    }

    public static PlayerDisconnectS2CPayload read(PacketByteBuf buf) {
        UUID uuid = buf.readUuid();
        return new PlayerDisconnectS2CPayload(uuid);
    }

}
