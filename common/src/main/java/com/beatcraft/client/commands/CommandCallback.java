package com.beatcraft.client.commands;

import net.minecraft.network.chat.Component;

public interface CommandCallback {
    void sendFeedback(Component fb);
}
