package com.beatcraft.client.commands;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.UUID;

public interface CommandCallback {
    void sendFeedback(Component fb);

    String getStringArg(String name);
    int getIntArg(String name);
    float getFloatArg(String name);
    double getDoubleArg(String name);
    Vector3f getVec3fArg(String name);
    UUID getUuidArg(String name);

    Level getLevel();
}
