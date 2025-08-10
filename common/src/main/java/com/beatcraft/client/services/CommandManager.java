package com.beatcraft.client.services;

import com.beatcraft.client.commands.CommandTree;

import java.util.ServiceLoader;

public class CommandManager {

    private static final ICommandManager inner = ServiceLoader.load(ICommandManager.class).findFirst().orElseThrow(() -> new RuntimeException("Failed to load Beatcraft Commands"));

    public static void register(CommandTree... commands) {
        inner.register(commands);
    }

}
