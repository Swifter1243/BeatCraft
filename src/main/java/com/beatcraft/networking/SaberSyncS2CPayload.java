package com.beatcraft.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;


public record SaberSyncS2CPayload(UUID player, Vector3f leftPos, Quaternionf leftRot, Vector3f rightPos, Quaternionf rightRot) implements CustomPayload {

    static PacketCodec<PacketByteBuf, SaberSyncS2CPayload> CODEC = CustomPayload.codecOf(SaberSyncS2CPayload::write, SaberSyncS2CPayload::read);
    static CustomPayload.Id<SaberSyncS2CPayload> ID = new Id<>(BeatCraftNetworking.SABER_SYNC_S2C_PAYLOAD);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static SaberSyncS2CPayload read(PacketByteBuf buf) {
        var leftPos = buf.readVector3f();
        var leftRot = buf.readQuaternionf();
        var rightPos = buf.readVector3f();
        var rightRot = buf.readQuaternionf();
        var player = buf.readUuid();
        return new SaberSyncS2CPayload(player, leftPos, leftRot, rightPos, rightRot);
    }

    public void write(PacketByteBuf buf) {
        buf.writeVector3f(leftPos);
        buf.writeQuaternionf(leftRot);
        buf.writeVector3f(rightPos);
        buf.writeQuaternionf(rightRot);
        buf.writeUuid(player);
    }
}