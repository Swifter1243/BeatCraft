package com.beatcraft;


import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.data.PlayerConfig;
import com.beatcraft.data.menu.SongData;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.menu.SongList;
import com.beatcraft.render.block.BlockRenderSettings;
import com.beatcraft.render.dynamic_loader.DynamicTexture;
import com.beatcraft.render.item.GeckolibRenderInit;
import com.beatcraft.replay.PlayRecorder;
import com.beatcraft.replay.Replayer;
import com.beatcraft.screen.SettingsScreen;
import com.beatcraft.screen.SongDownloaderScreen;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.compress.archivers.dump.UnrecognizedFormatException;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class BeatCraftClient implements ClientModInitializer {

    public static PlayerConfig playerConfig = PlayerConfig.loadFromFile();
    public static final SongList songs = new SongList();

    public static final KeyBinding settingsKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.beatcraft.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.beatcraft.keybindings"));
    public static final KeyBinding songSearchKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.beatcraft.song_search", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.beatcraft.keybindings"));

    public static final Vec3d playerCameraPosition = new Vec3d(2, 0.5, -4);
    public static final Quaternionf playerCameraRotation = new Quaternionf();

    public static final Vec3d playerGlobalPosition = new Vec3d(0, 0, 0);
    public static final Quaternionf playerGlobalRotation = new Quaternionf();


    public static final Vec3d playerSaberPosition = new Vec3d(0, 0, 0);
    public static final Quaternionf playerSaberRotation = new Quaternionf();

    @Override
    public void onInitializeClient() {

        setupFiles();

        registerCommands();

        BlockRenderSettings.init();
        GeckolibRenderInit.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (settingsKeyBind.wasPressed()) {
                var screen = new SettingsScreen(null);
                client.setScreen(screen);
                while (settingsKeyBind.wasPressed());
            }
            else if (songSearchKeybind.wasPressed()) {
                var screen = new SongDownloaderScreen(null);
                client.setScreen(screen);
                while (songSearchKeybind.wasPressed());
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            songs.loadSongs();
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> {
            DynamicTexture.unloadAllTextures();
        });

    }

    private void setupFiles() {
        String runDirectory = MinecraftClient.getInstance().runDirectory.getAbsolutePath();
        List<String> makeFolders = List.of(
            runDirectory + "/beatmaps/",          // for beatmaps
            runDirectory + "/beatcraft/",         // root directory for other data
            runDirectory + "/beatcraft/replay/",  // replay files
            runDirectory + "/beatcraft/temp/"     // for stuff like temporary images for song covers and audio previews
        );

        for (String folderPath : makeFolders) {
            File folder = new File(folderPath);
            if (!folder.exists()) {
                var ignored = folder.mkdirs();
            }
        }

    }

    private int songPlay(CommandContext<FabricClientCommandSource> context) {
        BeatmapPlayer.play();
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
        context.getSource().sendFeedback(Text.literal("Song unloaded!"));
        return 1;
    }

    private int songLoadFile(CommandContext<FabricClientCommandSource> context) {
        String path = StringArgumentType.getString(context, "path");
        path = trimPathQuotes(path);

        Replayer.reset();


        if (handleDifficultySetup(context, path) == 1) {
            BeatmapAudioPlayer.playAudioFromFile(BeatmapPlayer.currentInfo.getSongFilename());
            BeatmapPlayer.restart();
            GameLogicHandler.reset();
            return 1;
        } else {
            return -1;
        }
    }

    private int songList(CommandContext<FabricClientCommandSource> context) {


        return 1;
    }

    private int songRecord(CommandContext<FabricClientCommandSource> context) {
        PlayRecorder.outputFile = StringArgumentType.getString(context, "output_file");
        Replayer.runReplay = false;
        return 1;
    }

    private int songReplay(CommandContext<FabricClientCommandSource> context) {
        String file = StringArgumentType.getString(context, "replay_file");
        try {
            Replayer.loadReplay(file);
            return 1;
        } catch (IOException e) {
            context.getSource().sendError(Text.literal("Failed to load replay"));
            BeatCraft.LOGGER.error("Failed to load replay", e);
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

    public static int handleDifficultySetup(CommandContext<FabricClientCommandSource> context, String path) {
        try {
            BeatmapPlayer.setupDifficultyFromFile(path);
        } catch (UnrecognizedFormatException e) {
            context.getSource().sendError(Text.literal("That jawn is an unsupported version!"));
            //e.printStackTrace();
            BeatCraft.LOGGER.error("That map is an unsupported version! ", e);
            return -1;
        } catch (IOException e) {
            context.getSource().sendError(Text.literal("That path didn't exist or something!"));
            //e.printStackTrace();
            BeatCraft.LOGGER.error("File could not be found! ", e);
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

    private int colorFromFloats(CommandContext<FabricClientCommandSource> context) {
        float fr = FloatArgumentType.getFloat(context, "R");
        float fg = FloatArgumentType.getFloat(context, "G");
        float fb = FloatArgumentType.getFloat(context, "B");

        int r = (int) (fr * 255);
        int g = (int) (fg * 255);
        int b = (int) (fb * 255);

        int hex = r;
        hex <<= 8;
        hex += g;
        hex <<= 8;
        hex += b;

        String hexStr = Integer.toHexString(hex);

        context.getSource().sendFeedback(Text.literal(
                String.format(
                        "int RGB  : %s, %s, %s\nfloat RGB: %s, %s, %s\npacked color: %s\nhex code: %s",
                        r, g, b,
                        fr, fg, fb,
                        hex, hexStr
                )
        ));

        return 1;
    }

    private int colorFromIntegers(CommandContext<FabricClientCommandSource> context) {
        int r = IntegerArgumentType.getInteger(context, "R");
        int g = IntegerArgumentType.getInteger(context, "G");
        int b = IntegerArgumentType.getInteger(context, "B");

        float fr = r / 255.0f;
        float fg = g / 255.0f;
        float fb = b / 255.0f;

        int hex = r;
        hex <<= 8;
        hex += g;
        hex <<= 8;
        hex += b;

        String hexStr = Integer.toHexString(hex);

        context.getSource().sendFeedback(Text.literal(
                String.format(
                        "int RGB  : %s, %s, %s\nfloat RGB: %s, %s, %s\npacked color: %s\nhex code: %s",
                        r, g, b,
                        fr, fg, fb,
                        hex, hexStr
                )
        ));

        return 1;
    }

    private int colorFromHex(CommandContext<FabricClientCommandSource> context) {

        String hexStr = StringArgumentType.getString(context, "hex_code");

        try {
            int hex = Integer.parseInt(hexStr, 16);

            int r = (hex >> 16) & 0xFF;
            int g = (hex >> 8) & 0xFF;
            int b = hex & 0xFF;

            float fr = r / 255.0f;
            float fg = g / 255.0f;
            float fb = b / 255.0f;

            context.getSource().sendFeedback(Text.literal(
                    String.format(
                            "int RGB  : %s, %s, %s\nfloat RGB: %s, %s, %s\npacked color: %s\nhex code: %s",
                            r, g, b,
                            fr, fg, fb,
                            hex, hexStr
                    )
            ));

        } catch (NumberFormatException e) {
            context.getSource().sendError(Text.literal("Invalid hex: " + e.getMessage()));
        }
        return 1;
    }

    private int songLoad(CommandContext<FabricClientCommandSource> context) {
        String songName = StringArgumentType.getString(context, "song");
        String diffSet = StringArgumentType.getString(context, "difficulty_set");
        String diff = StringArgumentType.getString(context, "difficulty");

        Replayer.reset();

        List<SongData> filtered = songs.getFiltered(songName);

        if (filtered.isEmpty()) {
            context.getSource().sendError(Text.translatable("command.beatcraft.error.song_not_found"));
            return -1;
        }

        SongData song = filtered.getFirst();

        if (!song.getDifficultySets().contains(diffSet)) {
            context.getSource().sendError(Text.translatable("command.beatcraft.error.difficulty_set_not_found"));
            return -1;
        }

        if (!song.getDifficulties(diffSet).contains(diff)) {
            context.getSource().sendError(Text.translatable("command.beatcraft.error.difficulty_not_found"));
            return -1;
        }

        SongData.BeatmapInfo beatmapInfo = song.getBeatMapInfo(diffSet, diff);

        if (PlayRecorder.outputFile != null) {
            PlayRecorder.songName = songName;
            PlayRecorder.difficultySet = diffSet;
            PlayRecorder.difficulty = diff;
        }

        if (handleDifficultySetup(context, beatmapInfo.getBeatmapLocation().toString()) == 1) {
            BeatmapAudioPlayer.playAudioFromFile(BeatmapPlayer.currentInfo.getSongFilename());
            BeatmapPlayer.restart();
            GameLogicHandler.reset();
            return 1;
        } else {
            return -1;
        }

    }

    private CompletableFuture<Suggestions> songDifficultySuggestor(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder suggestionsBuilder) {
        String songName = StringArgumentType.getString(context, "song");
        String diffSet = StringArgumentType.getString(context, "difficulty_set");
        String diffName = suggestionsBuilder.getRemaining();
        List<SongData> filtered = songs.getFiltered(songName);

        if (filtered.isEmpty()) {
            //context.getSource().sendError(Text.translatable("command.beatcraft.error.song_not_found"));
            return suggestionsBuilder.buildFuture();
        }

        SongData data = filtered.getFirst();
        List<String> sets = data.getDifficultySets();
        ArrayList<String> filteredSets = new ArrayList<>();

        for (String set : sets) {
            if (set.contains(diffSet)) {
                filteredSets.add(set);
            }
        }

        if (filteredSets.isEmpty()) {
            //context.getSource().sendError(Text.translatable("command.beatcraft.error.difficulty_set_not_found"));
            return suggestionsBuilder.buildFuture();
        }

        String set = filteredSets.getFirst();

        List<String> diffs = data.getDifficulties(set);
        ArrayList<String> suggests = new ArrayList<>();
        for (String diff : diffs) {
            if (diff.contains(diffName)) {
                suggests.add(diff.contains(" ") ? "\"" + diff + "\"" : diff);
            }
        }

        if (suggests.isEmpty()) {
            //context.getSource().sendError(Text.translatable("command.beatcraft.error.difficulty_not_found"));
            return suggestionsBuilder.buildFuture();
        }

        suggests.forEach(suggestionsBuilder::suggest);
        return suggestionsBuilder.buildFuture();

    }

    private CompletableFuture<Suggestions> songDifficultySetSuggestor(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder suggestionsBuilder) {
        String songName = StringArgumentType.getString(context, "song");
        String diffSet = suggestionsBuilder.getRemaining();
        List<SongData> filtered = songs.getFiltered(songName);

        if (filtered.isEmpty()) {
            //context.getSource().sendError(Text.translatable("command.beatcraft.error.song_not_found"));
            return suggestionsBuilder.buildFuture();
        }

        SongData data = filtered.getFirst();
        List<String> sets = data.getDifficultySets();
        ArrayList<String> suggest = new ArrayList<>();

        for (String set : sets) {
            if (set.contains(diffSet)) {
                suggest.add(set.contains(" ") ? "\"" + set + "\"" : set);
            }
        }

        if (suggest.isEmpty()) {
            //context.getSource().sendError(Text.translatable("command.beatcraft.error.difficulty_set_not_found"));
            return suggestionsBuilder.buildFuture();
        }

        suggest.forEach(suggestionsBuilder::suggest);
        return suggestionsBuilder.buildFuture();

    }

    private CompletableFuture<Suggestions> songSuggestor(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder suggestionsBuilder) {

        String songName = suggestionsBuilder.getRemaining();

        List<SongData> filtered = songs.getFiltered(songName);

        for (SongData song : filtered) {
            suggestionsBuilder.suggest(song.getTitle().contains(" ") ? "\"" + song.getTitle() + "\"" : song.getTitle());
        }

        return suggestionsBuilder.buildFuture();
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
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
                    .then(literal("loadFile")
                            .then(argument("path", StringArgumentType.greedyString())
                                    .executes(this::songLoadFile)
                            )
                    )
                    .then(literal("scrub")
                            .then(argument("beats", FloatArgumentType.floatArg())
                                    .executes(this::songScrub)
                            )
                    )
                    .then(literal("list")
                            .then(argument("filter", StringArgumentType.greedyString())
                                .executes(this::songList)
                            )
                            .executes(this::songList)
                    )
                    .then(literal("load")
                            .then(argument("song", StringArgumentType.string()).suggests(this::songSuggestor)
                                    .then(argument("difficulty_set", StringArgumentType.string()).suggests(this::songDifficultySetSuggestor)
                                            .then(argument("difficulty", StringArgumentType.string()).suggests(this::songDifficultySuggestor)
                                                    .executes(this::songLoad)
                                            )
                                    )
                            )
                    )
                    .then(literal("record")
                            .then(argument("output_file", StringArgumentType.string())
                                    .executes(this::songRecord)
                            )
                    )
                    .then(literal("replay")
                            .then(argument("replay_file", StringArgumentType.string())
                                    .executes(this::songReplay)
                            )
                    )
            );
            dispatcher.register(literal("color_helper")
                    .then(literal("hex")
                            .then(argument("hex_code", StringArgumentType.word())
                                    .executes(this::colorFromHex)
                            )
                    )
                    .then(literal("intRGB")
                            .then(argument("R", IntegerArgumentType.integer(0, 255))
                                    .then(argument("G", IntegerArgumentType.integer(0, 255))
                                            .then(argument("B", IntegerArgumentType.integer(0, 255))
                                                    .executes(this::colorFromIntegers)
                                            )
                                    )
                            )
                    )
                    .then(literal("floatRGB")
                            .then(argument("R", FloatArgumentType.floatArg(0, 1))
                                    .then(argument("G", FloatArgumentType.floatArg(0, 1))
                                            .then(argument("B", FloatArgumentType.floatArg(0, 1))
                                                    .executes(this::colorFromFloats)
                                            )
                                    )
                            )
                    )
            );
        }));
    }




}