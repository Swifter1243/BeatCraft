package com.beatcraft.client.commands;

import com.beatcraft.client.beatmap.BeatmapManager;
import com.beatcraft.client.beatmap.BeatmapRenderer;
import com.beatcraft.client.services.CommandManager;
import com.beatcraft.common.data.map.SongData;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static com.beatcraft.client.commands.CommandTree.argument;
import static com.beatcraft.client.commands.CommandTree.literal;

public class ClientCommands {

    public static class CommandResult {
        public final Component msg;
        public final int state;

        private CommandResult(int state, Component msg) {
            this.state = state;
            this.msg = msg;
        }

        public static CommandResult ok() {
            return new CommandResult(1, null);
        }

        public static CommandResult ok(Component msg) {
            return new CommandResult(1, msg);
        }

        public static CommandResult err() {
            return new CommandResult(-1, null);
        }

        public static CommandResult err(Component msg) {
            return new CommandResult(-1, msg);
        }

    }

    private static CommandResult listBeatmaps(CommandCallback callback) {
        return CommandResult.ok(Component.literal(BeatmapManager.getMapsInfo()));
    }

    private static CommandResult createBeatmap(CommandCallback callback) {

        var pos = callback.getVec3fArg("position");
        var rot = callback.getFloatArg("rotation");

        var map = BeatmapManager.place(callback.getLevel(), pos, rot * Mth.DEG_TO_RAD, BeatmapRenderer.RenderStyle.DISTANCE);

        return CommandResult.ok(Component.literal(String.format("Placed beatmap: %s", map.getDisplayInfo())));
    }

    private static CompletableFuture<Suggestions> beatmapUuidSuggester(CommandCallback callback, SuggestionsBuilder builder) {
        var current = builder.getRemaining();
        for (var map : BeatmapManager.beatmaps) {
            var id = map.mapId.toString();
            if (id.contains(current)) {
                builder.suggest(id);
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> mapSuggester(CommandCallback callback, SuggestionsBuilder builder) {

        var song_name = builder.getRemaining();

        var matches = new ArrayList<SongData>();

        for (var song : BeatmapManager.songs) {
            if (song.getTitle().contains(song_name)) {
                matches.add(song);
            }
        }

        if (matches.isEmpty()) {
            return builder.buildFuture();
        }

        for (var m : matches) {
            builder.suggest("\"" + m.getTitle() + "\"");
        }

        return builder.buildFuture();
    }
    private static CompletableFuture<Suggestions> mapSetSuggester(CommandCallback callback, SuggestionsBuilder builder) {

        var song_name = callback.getStringArg("map");
        var set_name = builder.getRemaining();

        var matches = new ArrayList<SongData>();

        for (var song : BeatmapManager.songs) {
            if (song.getTitle().contains(song_name)) {
                matches.add(song);
            }
        }

        if (matches.isEmpty()) {
            return builder.buildFuture();
        }

        var song = matches.getFirst();

        var setMatches = new ArrayList<String>();

        for (var set : song.getDifficultySets()) {
            if (set.contains(set_name)) {
                setMatches.add(set);
            }
        }

        if (setMatches.isEmpty()) {
            return builder.buildFuture();
        }

        for (var s : setMatches) {
            builder.suggest("\"" + s + "\"");
        }

        return builder.buildFuture();
    }
    private static CompletableFuture<Suggestions> mapDifficultySuggester(CommandCallback callback, SuggestionsBuilder builder) {


        var song_name = callback.getStringArg("map");
        var set_name = callback.getStringArg("set");
        var diff_name = builder.getRemaining();

        var matches = new ArrayList<SongData>();

        for (var song : BeatmapManager.songs) {
            if (song.getTitle().contains(song_name)) {
                matches.add(song);
            }
        }

        if (matches.isEmpty()) {
            return builder.buildFuture();
        }

        var song = matches.getFirst();

        var setMatches = new ArrayList<String>();

        for (var set : song.getDifficultySets()) {
            if (set.contains(set_name)) {
                setMatches.add(set);
            }
        }

        if (setMatches.isEmpty()) {
            return builder.buildFuture();
        }

        var diffs = song.getDifficulties(setMatches.getFirst());

        var diffMatches = new ArrayList<String>();

        for (var diff : diffs) {
            if (diff.contains(diff_name)) {
                diffMatches.add(diff);
            }
        }

        if (diffMatches.isEmpty()) {
            return builder.buildFuture();
        }

        for (var diff : diffMatches) {
            builder.suggest("\"" + diff + "\"");
        }

        return builder.buildFuture();
    }

    private static CommandResult playSongForMap(CommandCallback callback) {

        var uuid = callback.getUuidArg("uuid");
        var map = callback.getStringArg("map");
        var set = callback.getStringArg("set");
        var diff = callback.getStringArg("difficulty");

        callback.sendFeedback(Component.literal(String.format("Trying to play map: %s %s %s", map, set, diff)));

        var controller = BeatmapManager.getByUuid(uuid);

        if (controller == null) {
            return CommandResult.err(Component.translatable("command.beatcraft.error.map_controller_not_found"));
        }

        var selectedBeatmap = BeatmapManager.songs.stream().filter(m -> m.getTitle().equals(map)).findFirst();

        if (selectedBeatmap.isEmpty()) {
            return CommandResult.err(Component.translatable("command.beatcraft.error.song_not_found"));
        }

        var song = selectedBeatmap.get();

        if (!song.getDifficultySets().contains(set)) {
            return CommandResult.err(Component.translatable("command.beatcraft.error.difficulty_set_not_found"));
        }

        if (!song.getDifficulties(set).contains(diff)) {
            return CommandResult.err(Component.translatable("command.beatcraft.error.difficulty_not_found"));
        }

        var mapInfo = song.getBeatMapInfo(set, diff);

        controller.playSong(mapInfo);

        return CommandResult.ok();
    }

    private static CommandResult setMapSpeed(CommandCallback callback) {
        var uuid = callback.getUuidArg("uuid");

        var controller = BeatmapManager.getByUuid(uuid);

        if (controller == null) {
            return CommandResult.err(Component.translatable("command.beatcraft.error.map_controller_not_found"));
        }

        var speed = callback.getFloatArg("value");

        if (0f >= speed || speed > 7f) {
            return CommandResult.err(Component.translatable("command.beatcraft.error.invalid_speed", 0, 7));
        }

        controller.setSpeed(speed);
        return CommandResult.ok(Component.translatable("command.beatcraft.feedback.set_speed", speed * 100f));
    }

    private static CommandResult mapSeek(CommandCallback callback) {

        var uuid = callback.getUuidArg("uuid");

        var controller = BeatmapManager.getByUuid(uuid);

        if (controller == null) {
            return CommandResult.err(Component.translatable("command.beatcraft.error.map_controller_not_found"));
        }

        var beat = callback.getFloatArg("beat");

        controller.seek(beat);

        return CommandResult.ok();
    }

    private static CommandResult resumeMap(CommandCallback callback) {

        var uuid = callback.getUuidArg("uuid");

        var controller = BeatmapManager.getByUuid(uuid);

        if (controller == null) {
            return CommandResult.err(Component.translatable("command.beatcraft.error.map_controller_not_found"));
        }

        controller.resume();

        return CommandResult.ok();
    }

    private static CommandResult pauseMap(CommandCallback callback) {

        var uuid = callback.getUuidArg("uuid");

        var controller = BeatmapManager.getByUuid(uuid);

        if (controller == null) {
            return CommandResult.err(Component.translatable("command.beatcraft.error.map_controller_not_found"));
        }

        controller.pause();

        return CommandResult.ok();
    }

    public static void init() {
        CommandManager.register(
            literal("beatmap").then(
                literal("list")
                    .executes(ClientCommands::listBeatmaps)
            ).then(
                literal("place").then(
                    argument("position", CommandTree.ArgumentType.Vec3i).then(
                        argument("rotation", CommandTree.ArgumentType.Float)
                            .executes(ClientCommands::createBeatmap)
                    )
                )
            ).then(
                argument("uuid", CommandTree.ArgumentType.Uuid).suggests(ClientCommands::beatmapUuidSuggester).then(
                    literal("play").then(
                        argument("map", CommandTree.ArgumentType.String).suggests(ClientCommands::mapSuggester).then(
                            argument("set", CommandTree.ArgumentType.String).suggests(ClientCommands::mapSetSuggester).then(
                                argument("difficulty", CommandTree.ArgumentType.String).suggests(ClientCommands::mapDifficultySuggester)
                                    .executes(ClientCommands::playSongForMap)
                            )
                        )
                    ).executes(ClientCommands::resumeMap)
                ).then(
                    literal("speed").then(
                        argument("value", CommandTree.ArgumentType.Float)
                            .executes(ClientCommands::setMapSpeed)
                    )
                ).then(
                    literal("seek").then(
                        argument("beat", CommandTree.ArgumentType.Float)
                            .executes(ClientCommands::mapSeek)
                    )
                ).then(
                    literal("pause")
                        .executes(ClientCommands::pauseMap)
                ).then(
                    literal("resume")
                        .executes(ClientCommands::resumeMap)
                )
            ).build()
        );
    }

}
