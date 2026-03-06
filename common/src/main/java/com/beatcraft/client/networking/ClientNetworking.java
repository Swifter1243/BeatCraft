package com.beatcraft.client.networking;

import com.beatcraft.networking.SetSaberModelPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

public class ClientNetworking {


    public static void sendSetSaberPacket(String id, boolean mainHand, boolean offHand) {
        var con = Minecraft.getInstance().getConnection();
        if (con != null) {
            byte x = (byte) ((mainHand ? 0b10 : 0) | (offHand ? 0b01 : 0));
            con.send(new ServerboundCustomPayloadPacket(new SetSaberModelPayload(id, x)));
        }
    }

}
