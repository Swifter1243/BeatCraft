package com.beatcraft;


import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.render.BeatmapPlayer;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.apache.commons.compress.archivers.dump.UnrecognizedFormatException;

import java.io.IOException;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class BeatCraftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerCommands();
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("playsong")
                .executes(context -> {
                    BeatmapPlayer.play();
                    context.getSource().sendFeedback(Text.literal("Song played"));
                    return 1;
                })
                .then(argument("beat", FloatArgumentType.floatArg(0)).executes(context -> {
                    float beat = FloatArgumentType.getFloat(context, "beat");
                    BeatmapPlayer.play(beat);

                    context.getSource().sendFeedback(Text.literal("Song played at beat " + beat));
                    return 1;
                }))));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("pausesong")
                .executes(context -> {
                    BeatmapPlayer.pause();
                    context.getSource().sendFeedback(Text.literal("Song paused"));
                    return 1;
                })));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("restartsong")
                .executes(context -> {
                    BeatmapPlayer.restart();
                    context.getSource().sendFeedback(Text.literal("Song restarted"));
                    return 1;
                })));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("songspeed")
                .then(argument("speed", FloatArgumentType.floatArg(0)).executes(context -> {
                    float speed = FloatArgumentType.getFloat(context, "speed");
                    BeatmapPlayer.setPlaybackSpeed(speed);
                    context.getSource().sendFeedback(Text.literal("Song speed set to " + speed + "!"));
                    return 1;
                }))));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("clearobjects")
                .executes(context -> {
                    BeatmapPlayer.currentBeatmap = null;
                    context.getSource().sendFeedback(Text.literal("Objects cleared!"));
                    return 1;
                })));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("loadmap")
                .then(argument("path", StringArgumentType.greedyString()).executes(context -> {
                    String path = StringArgumentType.getString(context, "path");

                    if (handleDifficultySetup(context, path) == 1) {
                        BeatmapAudioPlayer.beatmapAudio.closeBuffer();
                        BeatmapAudioPlayer.playAudioFromFile(BeatmapPlayer.currentInfo.getSongFilename());
                        BeatmapPlayer.restart();
                        return 1;
                    } else {
                        return -1;
                    }
                }))));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("scrubsong")
                .then(argument("beats", FloatArgumentType.floatArg()).executes(context -> {
                    float beats = FloatArgumentType.getFloat(context, "beats");
                    float newBeat = Math.max(0.0f, BeatmapPlayer.getCurrentBeat() + beats);
                    BeatmapPlayer.play(newBeat);

                    context.getSource().sendFeedback(Text.literal("Scrubbed to beat " + newBeat + "!"));
                    return 1;
                }))));
    }

    private int handleDifficultySetup(CommandContext<FabricClientCommandSource> context, String path) {
        try {
            BeatmapPlayer.setupDifficultyFromFile(path);
        } catch (UnrecognizedFormatException e) {
            context.getSource().sendError(Text.literal("That jawn is an unsupported version!"));
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            context.getSource().sendError(Text.literal("That path didn't exist or something!"));
            e.printStackTrace();
            return -1;
        }

        context.getSource().sendFeedback(Text.literal("Beatmap loaded!"));
        return 1;
    }

    private CutDirection getCutFromString(String cutDirection) {
        return switch (cutDirection) {
            case "up" -> CutDirection.UP;
            case "left" -> CutDirection.LEFT;
            case "right" -> CutDirection.RIGHT;
            case "up_left", "left_up", "upleft", "leftup" -> CutDirection.UP_LEFT;
            case "down_left", "left_down", "downleft", "leftdown" -> CutDirection.DOWN_LEFT;
            case "up_right", "right_up", "upright", "rightup" -> CutDirection.UP_RIGHT;
            case "down_right", "right_down", "downright", "rightdown" -> CutDirection.DOWN_RIGHT;
            case "dot" -> CutDirection.DOT;
            default -> CutDirection.DOWN;
        };
    }

}