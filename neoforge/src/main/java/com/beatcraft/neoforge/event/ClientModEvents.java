package com.beatcraft.neoforge.event;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.resources.ResourceReloadListener;
import com.beatcraft.neoforge.client.services.CommandManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = Beatcraft.MOD_ID)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerReloadEvent(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ResourceReloadListener());
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandManager.dispatcher = event.getDispatcher();
        BeatcraftClient.initCommands();
    }

}
