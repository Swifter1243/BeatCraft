package com.beatcraft.neoforge.client;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.neoforge.client.services.CommandManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@Mod(value = Beatcraft.MOD_ID, dist = Dist.CLIENT)
public class BeatcraftNeoforgeClient {
    public BeatcraftNeoforgeClient() {
        BeatcraftClient.earlyInit();
    }


}
