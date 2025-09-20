package com.beatcraft.fabric.client.command;

import com.beatcraft.client.commands.CommandCallback;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.UUID;

public record FabricCommandCallback(CommandContext<FabricClientCommandSource> ctx) implements CommandCallback {

    @Override
    public void sendFeedback(Component fb) {
        ctx.getSource().sendFeedback(fb);
    }

    public String getStringArg(String name) {
        return StringArgumentType.getString(ctx, name);
    }

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
        var p = ctx.getSource().getPosition();
        var c = new CommandSourceStack(null, p, ctx.getSource().getRotation(), null, 0, null, null, null, ctx.getSource().getEntity());

        return (ctx.getArgument(name, Coordinates.class)).getPosition(c).toVector3f();
    }

    @Override
    public UUID getUuidArg(String name) {
        return ctx.getArgument(name, UUID.class);
    }

    @Override
    public Level getLevel() {
        return ctx.getSource().getWorld();
    }
}
