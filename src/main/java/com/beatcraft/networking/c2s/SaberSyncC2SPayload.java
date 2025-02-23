package com.beatcraft.networking.c2s;

import com.beatcraft.networking.BeatCraftNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record SaberSyncC2SPayload(Vector3f leftPos, Quaternionf leftRot, Vector3f rightPos, Quaternionf rightRot) implements CustomPayload {

    public static PacketCodec<PacketByteBuf, SaberSyncC2SPayload> CODEC = CustomPayload.codecOf(SaberSyncC2SPayload::write, SaberSyncC2SPayload::read);
    public static CustomPayload.Id<SaberSyncC2SPayload> ID = new Id<>(BeatCraftNetworking.SABER_SYNC_C2S);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static SaberSyncC2SPayload read(PacketByteBuf buf) {
        var leftPos = buf.readVector3f();
        var leftRot = buf.readQuaternionf();
        var rightPos = buf.readVector3f();
        var rightRot = buf.readQuaternionf();

        return new SaberSyncC2SPayload(leftPos, leftRot, rightPos, rightRot);
    }

    public void write(PacketByteBuf buf) {
        buf.writeVector3f(leftPos);
        buf.writeQuaternionf(leftRot);
        buf.writeVector3f(rightPos);
        buf.writeQuaternionf(rightRot);
    }
}
