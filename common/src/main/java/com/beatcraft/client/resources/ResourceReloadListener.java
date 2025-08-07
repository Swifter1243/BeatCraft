package com.beatcraft.client.resources;

import com.beatcraft.Beatcraft;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Environment(EnvType.CLIENT)
public class ResourceReloadListener implements PreparableReloadListener {

    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {

        return CompletableFuture.runAsync(() -> {

            Minecraft.getInstance().execute(() -> {

                Beatcraft.LOGGER.info("Loading client resources for {}", Platform.isFabric() ? "Fabric" : "Neoforge");

            });

        });
    }
}
