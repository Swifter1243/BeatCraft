package com.beatcraft;


import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.base_providers.BaseProviderHandler;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.data.PlayerConfig;
import com.beatcraft.data.menu.SongData;
import com.beatcraft.debug.BeatCraftDebug;
import com.beatcraft.items.ModItems;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.InputSystem;
import com.beatcraft.menu.SongList;
import com.beatcraft.networking.BeatCraftClientNetworking;
import com.beatcraft.networking.c2s.MapSyncC2SPayload;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.render.block.BlockRenderSettings;
import com.beatcraft.render.dynamic_loader.DynamicTexture;
import com.beatcraft.render.effect.Bloomfog;
import com.beatcraft.render.instancing.InstancedMesh;
import com.beatcraft.render.item.ItemRenderSettings;
import com.beatcraft.render.item.SaberItemRenderer;
import com.beatcraft.render.lightshow_event_visualizer.EventVisualizer;
import com.beatcraft.replay.PlayRecorder;
import com.beatcraft.replay.ReplayHandler;
import com.beatcraft.replay.Replayer;
import com.beatcraft.screen.SettingsScreen;
import com.beatcraft.screen.SongDownloaderScreen;
import com.beatcraft.vivify.VivifyController;
import com.beatcraft.vivify.assetbundle.files.BundleFile;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.nbt.*;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.apache.commons.compress.archivers.dump.UnrecognizedFormatException;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class BeatCraftClient implements ClientModInitializer {

    public static Random random = Random.create();

    public static PlayerConfig playerConfig = null;
    public static final SongList songs = new SongList();

    public static final KeyBinding settingsKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.beatcraft.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.beatcraft.keybindings"));
    public static final KeyBinding songSearchKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.beatcraft.song_search", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.beatcraft.keybindings"));
    public static final KeyBinding pauseLevelKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.beatcraft.pause_song", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.beatcraft.keybindings"));
    public static final KeyBinding toggleFPFCKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.beatcraft.toggle_fpfc", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.beatcraft.keybindings"));
    public static final KeyBinding toggleMovementLock = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.beatcraft.lock_movement", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "category.beatcraft.keybindings"));

    public static final Vec3d playerCameraPosition = new Vec3d(0, 0, 0);
    public static final Quaternionf playerCameraRotation = new Quaternionf();

    public static final Vec3d playerGlobalPosition = new Vec3d(0, 0, 0);
    public static final Quaternionf playerGlobalRotation = new Quaternionf();

    public static final Vec3d playerSaberPosition = new Vec3d(0, 0, 0);
    public static final Quaternionf playerSaberRotation = new Quaternionf();

    public static int windowWidth = 0;
    public static int windowHeight = 0;

    @Override
    public void onInitializeClient() {

        setupFiles();

        registerCommands();

        BlockRenderSettings.init();
        ItemRenderSettings.init();

        BeatCraftClientNetworking.init();

        playerConfig = PlayerConfig.loadFromFile();

        BaseProviderHandler.setupDynamicProviders();

        VivifyController.init();

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new BeatCraftAssetReloadListener());

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            HUDRenderer.triggerPressed = false;
        });


        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            var window = MinecraftClient.getInstance().getWindow();
            var w = Math.max(1, window.getWidth());
            var h = Math.max(1, window.getHeight());

            if (w != windowWidth || h != windowHeight) {
                windowWidth = w;
                windowHeight = h;
                BeatCraftRenderer.updateBloomfogSize(window.getWidth(), window.getHeight());
            }

            if (settingsKeyBind.wasPressed()) {
                var screen = new SettingsScreen(null);
                client.setScreen(screen);
                while (settingsKeyBind.wasPressed());
            }
            if (songSearchKeybind.wasPressed()) {
                var screen = new SongDownloaderScreen(null);
                client.setScreen(screen);
                while (songSearchKeybind.wasPressed());
            }
            if (pauseLevelKeybind.wasPressed()) {
                if (!GameLogicHandler.isTrackingClient()) return;

                if (GameLogicHandler.isPaused()) {
                    GameLogicHandler.unpauseMap();
                } else if (BeatmapPlayer.isPlaying()) {
                    GameLogicHandler.pauseMap();
                }
                while (pauseLevelKeybind.wasPressed());
            }
            if (toggleFPFCKeybind.wasPressed()) {
                if (client.player != null) {
                    toggleFPFC();
                    client.player.sendMessage(Text.translatable(GameLogicHandler.FPFC ? "event.beatcraft.fpfc_enabled" : "event.beatcraft.fpfc_disabled"));
                    while (toggleFPFCKeybind.wasPressed());
                }
            }
            if (toggleMovementLock.wasPressed()) {
                if (client.player != null) {
                    if (InputSystem.isMovementLocked()) {
                        InputSystem.unlockMovement();
                        client.player.sendMessage(Text.translatable("event.beatcraft.movement_unlocked"));
                    } else {
                        InputSystem.lockMovement();
                        client.player.sendMessage(Text.translatable("event.beatcraft.movement_locked", toggleMovementLock.getBoundKeyLocalizedText().getString()));
                    }
                    while (toggleMovementLock.wasPressed()) ;
                }
            }
        });


        ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> {
            DynamicTexture.unloadAllTextures();
            InstancedMesh.cleanupAll();
            BeatCraftRenderer.bloomfog.unload();
            BeatmapAudioPlayer.unmuteVanillaMusic();
            playerConfig.writeToFile();
        });

        ClientPreAttackCallback.EVENT.register((client, player, c) -> {
            if (GameLogicHandler.isTrackingClient() && player.getMainHandStack().isOf(ModItems.SABER_ITEM)) {
                HUDRenderer.triggerPressed = true;
                HUDRenderer.pointerSaber = NoteType.BLUE;
                return true;
            }
            return false;
        });


    }

    private void setupFiles() {
        String runDirectory = MinecraftClient.getInstance().runDirectory.getAbsolutePath();
        List<String> makeFolders = List.of(
            runDirectory + "/beatmaps/",               // for beatmaps
            runDirectory + "/beatcraft/",              // root directory for other data
            runDirectory + "/beatcraft/replay/",       // replay files
            runDirectory + "/beatcraft/temp/",         // for stuff like temporary images for song covers and audio previews
            runDirectory + "/beatcraft/custom_sabers/" // for custom sabers
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
        GameLogicHandler.mapSpeed = 1;
        context.getSource().sendFeedback(Text.literal("Song speed reset! (1.0)"));
        return 1;
    }

    private int songSpeedScalar(CommandContext<FabricClientCommandSource> context) {
        float speed = FloatArgumentType.getFloat(context, "scalar");
        BeatmapPlayer.setPlaybackSpeed(speed);
        GameLogicHandler.mapSpeed = speed;
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
        BeatmapPlayer.setCurrentBeat(newBeat);


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

        //if (PlayRecorder.outputFile != null) {
        //    PlayRecorder.songID = songName;
        //    PlayRecorder.difficultySet = diffSet;
        //    PlayRecorder.difficulty = diff;
        //}

        if (handleDifficultySetup(context, beatmapInfo.getBeatmapLocation().toString()) == 1) {
            BeatmapAudioPlayer.playAudioFromFile(BeatmapPlayer.currentInfo.getSongFilename());
            BeatmapPlayer.restart();
            GameLogicHandler.reset();
            if (song.getId() != null) {
                ClientPlayNetworking.send(new MapSyncC2SPayload(song.getId(), diffSet, diff, BeatCraftClient.playerConfig.getActiveModifiers()));
            }
            HUDRenderer.scene = HUDRenderer.MenuScene.InGame;
            return 1;
        } else {
            return -1;
        }

    }

    private int enableFPFC(CommandContext<FabricClientCommandSource> context) {
        GameLogicHandler.FPFC = true;
        context.getSource().sendFeedback(Text.of("Enabled FPFC"));
        return 1;
    }

    private int disableFPFC(CommandContext<FabricClientCommandSource> context) {
        GameLogicHandler.FPFC = false;
        context.getSource().sendFeedback(Text.of("Disabled FPFC"));
        return 1;
    }
    private int toggleFPFC(CommandContext<FabricClientCommandSource> context) {
        toggleFPFC();
        context.getSource().sendFeedback(Text.of(GameLogicHandler.FPFC ? "Enabled FPFC" : "Disabled FPFC"));
        return 1;
    }

    private static void toggleFPFC() {
        GameLogicHandler.FPFC = !GameLogicHandler.FPFC;

    }


    private CompletableFuture<Suggestions> songDifficultySuggester(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder suggestionsBuilder) {
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

    private CompletableFuture<Suggestions> songDifficultySetSuggester(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder suggestionsBuilder) {
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

    private CompletableFuture<Suggestions> songSuggester(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder suggestionsBuilder) {

        String songName = suggestionsBuilder.getRemaining();

        List<SongData> filtered = songs.getFiltered(songName);

        for (SongData song : filtered) {
            suggestionsBuilder.suggest(song.getTitle().contains(" ") ? "\"" + song.getTitle() + "\"" : song.getTitle());
        }

        return suggestionsBuilder.buildFuture();
    }

    private int setDebugValue(CommandContext<FabricClientCommandSource> context) {
        var key = StringArgumentType.getString(context, "key");

        var ty = StringArgumentType.getString(context, "type");

        var value = StringArgumentType.getString(context, "value");

        Object val = value;

        switch (ty) {
            case "vec3" -> {
                var comps = value.split("(, *| +)");
                if (comps.length == 1) {
                    var d = Float.parseFloat(comps[0]);
                    val = new Vector3f(d);
                } else if (comps.length == 3) {
                    var x = Float.parseFloat(comps[0]);
                    var y = Float.parseFloat(comps[1]);
                    var z = Float.parseFloat(comps[2]);
                    val = new Vector3f(x, y, z);
                } else {
                    context.getSource().sendError(Text.of("Failed to parse Vector3f. must have either 1 or 3 float components"));
                    return -1;
                }
            }
            case "quat" -> {
                var comps = value.split("(, *| +)");
                if (comps.length == 4) {
                    var x = Float.parseFloat(comps[0]);
                    var y = Float.parseFloat(comps[1]);
                    var z = Float.parseFloat(comps[2]);
                    var w = Float.parseFloat(comps[3]);
                    val = new Quaternionf(x, y, z, w);
                }
            }
            case "f" -> {
                val = Float.parseFloat(value);
            }
            case "i" -> {
                val = Integer.parseInt(value);
            }
            case "b" -> {
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("t")) {
                    val = true;
                } else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("f")) {
                    val = false;
                }
            }
            case "d" -> {
                val = Double.parseDouble(value);
            }
            case "l" -> {
                val = Long.parseLong(value);
            }
        }

        BeatCraftDebug.bindValue(key, val);

        return 1;
    }


    private int getDebugValue(CommandContext<FabricClientCommandSource> context) {
        var key = StringArgumentType.getString(context, "key");

        var value = BeatCraftDebug.getValue(key);

        context.getSource().sendFeedback(Text.literal(String.format("%s is currently: %s", key, value)));

        return 1;
    }


    private int removeDebugValue(CommandContext<FabricClientCommandSource> context) {
        var key = StringArgumentType.getString(context, "key");

        BeatCraftDebug.removeValue(key);

        context.getSource().sendFeedback(Text.of(String.format("Removed '%s'", key)));

        return 1;
    }

    private int startEventVisualizer(CommandContext<FabricClientCommandSource> context) {
        EventVisualizer.refresh();
        return 1;
    }

    private int setEventVisualizerLookahead(CommandContext<FabricClientCommandSource> context) {
        playerConfig.setDebugLightshowLookAhead(IntegerArgumentType.getInteger(context, "distance"));
        EventVisualizer.refresh();
        return 1;
    }

    private int setEventVisualizerLookbehind(CommandContext<FabricClientCommandSource> context) {
        playerConfig.setDebugLightshowLookBehind(IntegerArgumentType.getInteger(context, "distance"));
        EventVisualizer.refresh();
        return 1;
    }

    private int setEventVisualizerSpacing(CommandContext<FabricClientCommandSource> context) {
        playerConfig.setDebugLightshowBeatSpacing(IntegerArgumentType.getInteger(context, "size"));
        EventVisualizer.refresh();
        return 1;
    }

    private int selectSaber(CommandContext<FabricClientCommandSource> context) {
        var selector = NbtCompoundArgumentType.getNbtCompound(context, "data");

        var name = selector.getString("name");
        var auths = selector.getList("authors", NbtElement.STRING_TYPE);
        var authors = auths.stream().map(NbtElement::asString).toList();

        var found = SaberItemRenderer.selectModel(name, authors);

        if (!found) {
            context.getSource().sendFeedback(Text.literal("failed to load model"));
        }

        return 1;
    }

    private int listSabers(CommandContext<FabricClientCommandSource> context) {

        for (var model : SaberItemRenderer.models) {

            var auths = String.join("§f, §d", model.authors);

            MutableText message = Text.literal("§a" + model.modelName + "§f [§d" + auths + "§f]");

            var nbt = new NbtCompound();
            nbt.putString("name", model.modelName);
            var ls = new NbtList();
            for (var auth : model.authors) {
                ls.add(NbtString.of(auth));
            }
            nbt.put("authors", ls);

             message.fillStyle(message.getStyle()
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbt.asString()))
                 .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to copy selection data to clipboard")))
             );

             context.getSource().sendFeedback(message);
        }

        return 1;
    }

    private int reloadSaberModels(CommandContext<FabricClientCommandSource> context) {
        SaberItemRenderer.init();
        context.getSource().sendFeedback(Text.literal("Reloaded saber models"));
        return 1;
    }

    private int listBundleAssets(CommandContext<FabricClientCommandSource> context) {
        var path = StringArgumentType.getString(context, "asset_path");
        var bundle = BundleFile.tryLoadBundle(path);
        if (bundle != null) {
            for (var asset : bundle.getAssets()) {
                BeatCraft.LOGGER.info("Found asset: {}", asset.getName());
            }
        }
        // TODO: list assets further than just the logger.info in the loading process
        return 1;
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
                    .then(literal("load")
                            .then(argument("song", StringArgumentType.string()).suggests(this::songSuggester)
                                    .then(argument("difficulty_set", StringArgumentType.string()).suggests(this::songDifficultySetSuggester)
                                            .then(argument("difficulty", StringArgumentType.string()).suggests(this::songDifficultySuggester)
                                                    .executes(this::songLoad)
                                            )
                                    )
                            )
                    )
                    //.then(literal("record")
                    //        .then(argument("output_file", StringArgumentType.string())
                    //                .executes(this::songRecord)
                    //        )
                    //)
                    //.then(literal("replay")
                    //        .then(argument("replay_file", StringArgumentType.string())
                    //                .executes(this::songReplay)
                    //        )
                    //)
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
            dispatcher.register(literal("fpfc")
                .then(literal("enable").executes(this::enableFPFC))
                .then(literal("disable").executes(this::disableFPFC))
                .executes(this::toggleFPFC)
            );
            dispatcher.register(literal("bcdebug")
                .then(literal("set")
                    .then(argument("key", StringArgumentType.string())
                        .then(argument("type", StringArgumentType.string())
                            .then(argument("value", StringArgumentType.string())
                                .executes(this::setDebugValue)
                            )
                        )
                    )
                )
                .then(literal("get")
                    .then(argument("key", StringArgumentType.greedyString())
                        .executes(this::getDebugValue)
                    )
                )
                .then(literal("remove")
                    .then(argument("key", StringArgumentType.greedyString())
                        .executes(this::removeDebugValue)
                    )
                )
            );
            dispatcher.register(literal("event_renderer")
                .then(literal("start")
                    .executes(this::startEventVisualizer)
                )
                .then(literal("lookahead")
                    .then(argument("distance", IntegerArgumentType.integer(0))
                        .executes(this::setEventVisualizerLookahead)
                    )
                )
                .then(literal("lookbehind")
                    .then(argument("distance", IntegerArgumentType.integer(0))
                        .executes(this::setEventVisualizerLookbehind)
                    )
                )
                .then(literal("spacing")
                    .then(argument("size", IntegerArgumentType.integer(0))
                        .executes(this::setEventVisualizerSpacing)
                    )
                )
            );
            dispatcher.register(literal("custom_sabers")
                .then(literal("select")
                    .then(argument("data", NbtCompoundArgumentType.nbtCompound())
                        .executes(this::selectSaber)
                    )
                )
                .then(literal("list")
                    .executes(this::listSabers)
                )
                .then(literal("refresh")
                    .executes(this::reloadSaberModels)
                )
            );
            dispatcher.register(literal("vivify")
                .then(literal("bundle")
                    .then(literal("list-assets")
                        .then(argument("asset_path", StringArgumentType.greedyString())
                            .executes(this::listBundleAssets)
                        )
                    )
                )
            );
        }));
    }




}