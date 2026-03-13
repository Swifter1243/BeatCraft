package com.beatcraft.fabric.client;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.resources.ResourceReloadListener;
import com.beatcraft.fabric.client.render.item.ItemRenderSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class BeatcraftFabricClient implements ClientModInitializer {

    private static class ReloadWrapper implements IdentifiableResourceReloadListener {
        private final ResourceReloadListener reloader = new ResourceReloadListener();

        @Override
        public ResourceLocation getFabricId() {
            return ResourceLocation.tryBuild(Beatcraft.MOD_ID, "resource_reloader");
        }

        @Override
        public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
            return reloader.reload(preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2);
        }
    }

    @Override
    public void onInitializeClient() {
        BeatcraftClient.earlyInit();
        BeatcraftClient.initCommands();

        ItemRenderSettings.init();

        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            if (mc.player != null) {
                BeatcraftClient.playerPos = mc.player.position().toVector3f();
            }
        });

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ReloadWrapper());

    }
}
