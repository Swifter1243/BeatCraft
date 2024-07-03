package com.beatcraft;


import com.beatcraft.beatmap.BeatmapPlayer;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class BeatCraftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerCommands();
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("song-play")
                .executes(context -> {
                    context.getSource().sendFeedback(Text.literal("Song played"));
                    return 1;
                })
                .then(argument("beat", FloatArgumentType.floatArg(0)).executes(context -> {
                    float beat = FloatArgumentType.getFloat(context, "beat");
                    BeatmapPlayer.play(beat);

                    context.getSource().sendFeedback(Text.literal("Song played at beat " + beat));
                    return 1;
                }))));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("song-pause")
                .executes(context -> {
                            BeatmapPlayer.pause();
                            context.getSource().sendFeedback(Text.literal("Song paused"));
                            return 1;
                        }
                )));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("song-restart")
                .executes(context -> {
                            BeatmapPlayer.restart();
                            context.getSource().sendFeedback(Text.literal("Song restarted"));
                            return 1;
                        }
                )));
    }
}