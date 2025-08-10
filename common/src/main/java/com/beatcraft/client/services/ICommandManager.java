package com.beatcraft.client.services;

import com.beatcraft.client.commands.CommandTree;

public interface ICommandManager {

    void register(CommandTree... commands);

}
