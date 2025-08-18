package com.beatcraft.neoforge.client;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = Beatcraft.MOD_ID, dist = Dist.CLIENT)
public class BeatcraftNeoforgeClient {
    public BeatcraftNeoforgeClient() {
        BeatcraftClient.earlyInit();
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(BeatcraftClient::initCommands);

    }

}
