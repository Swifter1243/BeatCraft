package com.beatcraft.networking.s2c;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SceneSyncS2CPayload(byte scene) implements CustomPayload {
    public static Id<SceneSyncS2CPayload> ID = new Id<>(BeatCraftNetworking.SCENE_SYNC_S2C);
    public static PacketCodec<PacketByteBuf, SceneSyncS2CPayload> CODEC = PacketCodec.of(SceneSyncS2CPayload::write, SceneSyncS2CPayload::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeByte(scene);
    }

    public static SceneSyncS2CPayload read(PacketByteBuf buf) {
        return new SceneSyncS2CPayload(buf.readByte());
    }

}
