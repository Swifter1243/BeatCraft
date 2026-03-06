package com.beatcraft.networking;

import com.beatcraft.common.data.components.ModComponents;
import com.beatcraft.common.items.ModItems;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class ServerNetworking {
    
    public static void handleSetSaberModel(SetSaberModelPayload payload, Player player, Consumer<CustomPacketPayload> sendPacket) {

        var id = payload.id();
        var main = (payload.targets() & 0b01) > 0;
        var off = (payload.targets() & 0b10) > 0;

        if (off) {
            var l = player.getItemInHand(InteractionHand.OFF_HAND);
            if (l.is(ModItems.SABER_ITEM)) {
                l.set(ModComponents.SABER_MODEL.get(), id);
            }
        }

        if (main) {
            var l = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (l.is(ModItems.SABER_ITEM)) {
                l.set(ModComponents.SABER_MODEL.get(), id);
            }
        }

    }
    
}
