package com.beatcraft.fabric.client.command;

import com.beatcraft.client.commands.CommandCallback;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public record FabricCommandCallback(CommandContext<FabricClientCommandSource> ctx) implements CommandCallback {

    @Override
    public void sendFeedback(Component fb) {

    }
}
