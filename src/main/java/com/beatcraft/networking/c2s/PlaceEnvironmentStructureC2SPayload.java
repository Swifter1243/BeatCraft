package com.beatcraft.networking.c2s;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record PlaceEnvironmentStructureC2SPayload(String struct) implements CustomPayload {
    public static PacketCodec<PacketByteBuf, PlaceEnvironmentStructureC2SPayload> CODEC = CustomPayload.codecOf(
        PlaceEnvironmentStructureC2SPayload::write,
        PlaceEnvironmentStructureC2SPayload::read
    );

    public static CustomPayload.Id<PlaceEnvironmentStructureC2SPayload> ID = new Id<>(
        BeatCraftNetworking.PLACE_ENVIRONMENT_C2S
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static PlaceEnvironmentStructureC2SPayload read(PacketByteBuf buf) {
        return new PlaceEnvironmentStructureC2SPayload(buf.readString());
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(struct);
    }
}
