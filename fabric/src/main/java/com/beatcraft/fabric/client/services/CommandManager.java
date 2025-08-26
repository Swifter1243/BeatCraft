package com.beatcraft.fabric.client.services;

import com.beatcraft.client.commands.CommandTree;
import com.beatcraft.client.services.ICommandManager;
import com.beatcraft.fabric.client.command.FabricCommandCallback;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;

public class CommandManager implements ICommandManager {

    @Override
    public void register(CommandTree... commands) {
        ClientCommandRegistrationCallback.EVENT.register(((commandDispatcher, commandBuildContext) -> {
            for (var cmd : commands) {
                var builder = parse(cmd);

                commandDispatcher.register((LiteralArgumentBuilder<FabricClientCommandSource>) builder);
            }
        }));
    }

    private ArgumentBuilder<FabricClientCommandSource, ?> parse(CommandTree cmd) {
        // branch:
        // <name> : literal || arg-type
        // [branches...]
        // <exec>

        ArgumentBuilder<FabricClientCommandSource, ?> builder;

        if (cmd.type == CommandTree.ArgumentType.Literal) {
            builder = ClientCommandManager.literal(cmd.name);
        } else {
            builder = ClientCommandManager.argument(cmd.name, getArg(cmd.type));

            if (cmd.suggestionProvider != null) {
                ((RequiredArgumentBuilder<FabricClientCommandSource, ?>) builder).suggests((ctx, suggestionsBuilder) -> {
                    var callback = new FabricCommandCallback(ctx);

                    return cmd.suggestionProvider.apply(callback, suggestionsBuilder);
                });
            }

        }


        if (cmd.callback != null) {
            builder.executes(ctx -> {
                var cb = new FabricCommandCallback(ctx);

                var fb = cmd.callback.apply(cb);

                if (fb.msg != null) {
                    if (fb.state >= 0) {
                        ctx.getSource().sendFeedback(fb.msg);
                    } else {
                        ctx.getSource().sendError(fb.msg);
                    }
                }

                return fb.state;
            });
        }

        for (var subBranch : cmd.branches) {
            builder.then(parse(subBranch));
        }

        return builder;
    }

    private ArgumentType<?> getArg(CommandTree.ArgumentType type) {
        return switch (type) {
            case Literal -> throw new RuntimeException("Tried to parse literal as argument"); // unreachable
            case Integer -> IntegerArgumentType.integer();
            case Float -> FloatArgumentType.floatArg();
            case Vec3i -> Vec3Argument.vec3(false);
            case Vec3f -> Vec3Argument.vec3(false);
            case Vec2i -> Vec2Argument.vec2(false);
            case Vec2f -> Vec2Argument.vec2(false);
            case Uuid -> UuidArgument.uuid();
            case String -> StringArgumentType.string();
        };
    }

}
