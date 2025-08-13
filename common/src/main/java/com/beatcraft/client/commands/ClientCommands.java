package com.beatcraft.client.commands;

import com.beatcraft.client.beatmap.BeatmapManager;
import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.client.beatmap.BeatmapRenderer;
import com.beatcraft.client.services.CommandManager;
import net.minecraft.network.chat.Component;

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

        var map = BeatmapManager.place(callback.getLevel(), pos, rot, BeatmapRenderer.RenderStyle.DISTANCE);

        return CommandResult.ok(Component.literal(String.format("Placed beatmap: %s", map.getDisplayInfo())));
    }

    public static void init() {
        CommandManager.register(
            literal("beatmap").then(
                literal("list").executes(ClientCommands::listBeatmaps)
            ).then(
                literal("place").then(
                    argument("position", CommandTree.ArgumentType.Vec3i).then(
                        argument("rotation", CommandTree.ArgumentType.Float)
                            .executes(ClientCommands::createBeatmap)
                    ).build()
                ).build()
            )
            .build()
        );
    }

}
