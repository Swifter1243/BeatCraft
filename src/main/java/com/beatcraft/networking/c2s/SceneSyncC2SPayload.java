package com.beatcraft.networking.c2s;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SceneSyncC2SPayload(byte scene) implements CustomPayload {
    public static Id<SceneSyncC2SPayload> ID = new Id<>(BeatCraftNetworking.SCENE_SYNC_C2S);
    public static PacketCodec<PacketByteBuf, SceneSyncC2SPayload> CODEC = PacketCodec.of(SceneSyncC2SPayload::write, SceneSyncC2SPayload::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeByte(scene);
    }

    public static SceneSyncC2SPayload read(PacketByteBuf buf) {
        return new SceneSyncC2SPayload(buf.readByte());
    }

}
