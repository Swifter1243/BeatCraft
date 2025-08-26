package com.beatcraft.client.commands;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CommandTree {

    public class CommandArgumentBuilder {
        public CommandArgumentBuilder then(CommandTree branch) {
            CommandTree.this.branches.add(branch);
            return this;
        }

        public CommandArgumentBuilder then(CommandArgumentBuilder unBuiltBranch) {
            return then(unBuiltBranch.build());
        }

        public CommandArgumentBuilder suggests(BiFunction<CommandCallback, SuggestionsBuilder, CompletableFuture<Suggestions>> suggestionProvider) {
            CommandTree.this.suggestionProvider = suggestionProvider;
            return this;
        }

        public CommandTree executes(Function<CommandCallback, ClientCommands.CommandResult> command) {
            CommandTree.this.callback = command;
            return CommandTree.this;
        }

        public CommandTree build() {
            return CommandTree.this;
        }

    }

    public enum ArgumentType {
        Literal,
        Integer,
        Float,
        Vec3i,
        Vec3f,
        Vec2i,
        Vec2f,
        Uuid,
        String,
    }

    public final String name;
    public final ArgumentType type;
    public final ArrayList<CommandTree> branches;
    public Function<CommandCallback, ClientCommands.CommandResult> callback;
    public BiFunction<CommandCallback, SuggestionsBuilder, CompletableFuture<Suggestions>> suggestionProvider;

    private CommandTree(String name, ArgumentType type, ArrayList<CommandTree> branches) {
        this.name = name;
        this.type = type;
        this.branches = branches;
    }

    public static CommandArgumentBuilder literal(String l) {
        var b = new CommandTree(l, ArgumentType.Literal, new ArrayList<>());
        return b.new CommandArgumentBuilder();
    }

    public static CommandArgumentBuilder argument(String name, ArgumentType type) {
        var b = new CommandTree(name, type, new ArrayList<>());
        return b.new CommandArgumentBuilder();
    }



}
