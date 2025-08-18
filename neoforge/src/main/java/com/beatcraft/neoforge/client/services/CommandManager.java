package com.beatcraft.neoforge.client.services;

import com.beatcraft.client.commands.CommandTree;
import com.beatcraft.client.services.ICommandManager;
import com.beatcraft.neoforge.client.command.NeoforgeCommandCallback;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.neoforged.neoforge.client.ClientCommandHandler;

public class CommandManager implements ICommandManager {

    @Override
    public void register(CommandTree... commands) {

        CommandDispatcher<CommandSourceStack> dispatcher = ClientCommandHandler.getDispatcher();

        for (var cmd : commands) {
            dispatcher.register((LiteralArgumentBuilder<CommandSourceStack>) parse(cmd));
        }
    }

    private ArgumentBuilder<CommandSourceStack, ?> parse(CommandTree cmd) {

        ArgumentBuilder<CommandSourceStack, ?> builder;

        if (cmd.type == CommandTree.ArgumentType.Literal) {
            builder = LiteralArgumentBuilder.literal(cmd.name);
        } else {
            builder = RequiredArgumentBuilder.argument(cmd.name, getArg(cmd.type));
        }

        if (cmd.callback != null) {
            builder.executes(ctx -> {
                var cb = new NeoforgeCommandCallback(ctx);

                var fb = cmd.callback.apply(cb);

                if (fb.state == 0) {
                    ctx.getSource().sendSystemMessage(fb.msg);
                } else {
                    ctx.getSource().sendFailure(fb.msg);
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
        };
    }


}
