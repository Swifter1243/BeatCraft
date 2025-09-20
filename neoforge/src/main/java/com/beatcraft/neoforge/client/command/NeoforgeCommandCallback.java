package com.beatcraft.neoforge.client.command;

import com.beatcraft.client.commands.CommandCallback;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.UUID;

public record NeoforgeCommandCallback(CommandContext<CommandSourceStack> ctx) implements CommandCallback {
    @Override
    public void sendFeedback(Component fb) {
        ctx.getSource().sendSystemMessage(fb);
    }

    @Override
    public String getStringArg(String name) {
        return StringArgumentType.getString(ctx, name);
    }

    @Override
    public int getIntArg(String name) {
        return IntegerArgumentType.getInteger(ctx, name);
    }

    @Override
    public float getFloatArg(String name) {
        return FloatArgumentType.getFloat(ctx, name);
    }

    @Override
    public double getDoubleArg(String name) {
        return DoubleArgumentType.getDouble(ctx, name);
    }

    @Override
    public Vector3f getVec3fArg(String name) {
        return Vec3Argument.getVec3(ctx, name).toVector3f();
    }

    @Override
    public UUID getUuidArg(String name) {
        return UuidArgument.getUuid(ctx, name);
    }

    @Override
    public Level getLevel() {
        return ctx.getSource().getUnsidedLevel();
    }
}
