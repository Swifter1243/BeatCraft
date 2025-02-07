package com.beatcraft.networking;

import com.beatcraft.items.ModItems;
import com.beatcraft.render.effect.SaberRenderer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

import java.util.UUID;

public class BeatCraftClientNetworking {


    public static void init() {



        // receivers
        ClientPlayNetworking.registerGlobalReceiver(SaberSyncS2CPayload.ID, BeatCraftClientNetworking::handleSaberSyncPayload);


    }


    private static void handleSaberSyncPayload(SaberSyncS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            UUID uuid = payload.player();

            ClientWorld world = context.client().world;

            if (world == null) return;

            PlayerEntity player = world.getPlayerByUuid(uuid);

            if (player == null) return;

            ItemStack stack = player.getMainHandStack();
            ItemStack stack2 = player.getOffHandStack();

            if (player.getMainArm() == Arm.LEFT) {
                var s3 = stack;
                stack = stack2;
                stack2 = s3;
            }

            if (stack.isOf(ModItems.SABER_ITEM)) {
                SaberRenderer.renderReplaySaber(stack, payload.rightPos(), payload.rightRot());
            }

            if (stack2.isOf(ModItems.SABER_ITEM)) {
                SaberRenderer.renderReplaySaber(stack2, payload.leftPos(), payload.leftRot());
            }

        });
    }

}
