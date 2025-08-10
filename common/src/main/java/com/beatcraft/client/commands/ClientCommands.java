package com.beatcraft.client.commands;

import com.beatcraft.client.beatmap.BeatmapManager;
import com.beatcraft.client.services.CommandManager;
import net.minecraft.network.chat.Component;

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

    public static void init() {
        CommandManager.register(
            literal("beatmap").then(
                literal("list").executes(ClientCommands::listBeatmaps)
            )
            .build()
        );
    }

}
