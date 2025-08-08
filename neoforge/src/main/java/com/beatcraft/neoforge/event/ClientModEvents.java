package com.beatcraft.neoforge.event;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.resources.ResourceReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = Beatcraft.MOD_ID)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerReloadEvent(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ResourceReloadListener());
    }

}
