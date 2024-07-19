package com.beatcraft;


import com.beatcraft.audio.BeatmapAudioPlayer;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import org.apache.commons.compress.archivers.dump.UnrecognizedFormatException;
import java.io.IOException;
import static com.beatcraft.BeatmapPlayer.currentMusicVolume;
import static com.beatcraft.BeatmapPlayer.options;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class BeatCraftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerCommands();

    }


    private int songPlay(CommandContext<FabricClientCommandSource> context) {
        BeatmapPlayer.play();
        options.getSoundVolumeOption(SoundCategory.MUSIC).setValue(0.0);
        context.getSource().sendFeedback(Text.literal("Song played"));
        return 1;
    }

    private int songPlayBeat(CommandContext<FabricClientCommandSource> context) {
        float beat = FloatArgumentType.getFloat(context, "beat");
        BeatmapPlayer.play(beat);

        context.getSource().sendFeedback(Text.literal("Song played at beat " + beat));
        return 1;
    }

    private int songPause(CommandContext<FabricClientCommandSource> context) {
        BeatmapPlayer.pause();
        options.getSoundVolumeOption(SoundCategory.MUSIC).setValue(currentMusicVolume);
        context.getSource().sendFeedback(Text.literal("Song paused"));
        return 1;
    }

    private int songRestart(CommandContext<FabricClientCommandSource> context) {
        BeatmapPlayer.restart();
        context.getSource().sendFeedback(Text.literal("Song restarted"));
        return 1;
    }

    private int songSpeedReset(CommandContext<FabricClientCommandSource> context) {
        BeatmapPlayer.setPlaybackSpeed(1);
        context.getSource().sendFeedback(Text.literal("Song speed reset! (1.0)"));
        return 1;
    }

    private int songSpeedScalar(CommandContext<FabricClientCommandSource> context) {
        float speed = FloatArgumentType.getFloat(context, "scalar");
        BeatmapPlayer.setPlaybackSpeed(speed);
        context.getSource().sendFeedback(Text.literal("Song speed set to " + speed + "!"));
        return 1;
    }

    private int songUnload(CommandContext<FabricClientCommandSource> context) {
        BeatmapPlayer.currentBeatmap = null;
        BeatmapPlayer.currentInfo = null;
        BeatmapAudioPlayer.unload();
        options.getSoundVolumeOption(SoundCategory.MUSIC).setValue(currentMusicVolume);
        context.getSource().sendFeedback(Text.literal("Song unloaded!"));
        return 1;
    }

    private int songLoad(CommandContext<FabricClientCommandSource> context) {
        String path = StringArgumentType.getString(context, "path");
        path = trimPathQuotes(path);

        if (handleDifficultySetup(context, path) == 1) {
            BeatmapAudioPlayer.playAudioFromFile(BeatmapPlayer.currentInfo.getSongFilename());
            BeatmapPlayer.restart();
            return 1;
        } else {
            return -1;
        }
    }

    private String trimPathQuotes(String path) {
        if (path.startsWith("\"") && path.endsWith("\"")) {
            return path.substring(1, path.length() - 1);
        }
        else {
            return path;
        }
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

    private int songScrub(CommandContext<FabricClientCommandSource> context) {
        float beats = FloatArgumentType.getFloat(context, "beats");
        float newBeat = Math.max(0.0f, BeatmapPlayer.getCurrentBeat() + beats);
        BeatmapPlayer.play(newBeat);

        context.getSource().sendFeedback(Text.literal("Scrubbed to beat " + newBeat + "!"));
        return 1;
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) ->
                dispatcher.register(literal("song")
                        .then(literal("play")
                                .executes(this::songPlay)
                                .then(argument("beat", FloatArgumentType.floatArg(0))
                                        .executes(this::songPlayBeat)
                                )
                        )
                        .then(literal("pause")
                                .executes(this::songPause)
                        )
                        .then(literal("restart")
                                .executes(this::songRestart)
                        )
                        .then(literal("speed")
                                .then(literal("reset")
                                        .executes(this::songSpeedReset)
                                )
                                .then(argument("scalar", FloatArgumentType.floatArg(0.0001f, 5))
                                        .executes(this::songSpeedScalar)
                                )
                        )
                        .then(literal("unload")
                                .executes(this::songUnload)
                        )
                        .then(literal("load")
                                .then(argument("path", StringArgumentType.greedyString())
                                        .executes(this::songLoad)
                                )
                        )
                        .then(literal("scrub")
                                .then(argument("beats", FloatArgumentType.floatArg())
                                        .executes(this::songScrub)
                                )
                        )
                )));
    }
}