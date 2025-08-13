package com.beatcraft.client;

import com.beatcraft.client.commands.ClientCommands;
import com.beatcraft.common.data.PlayerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class BeatcraftClient {


    public static RandomSource random = RandomSource.create();
    public static PlayerConfig playerConfig;
    public static boolean wearingHeadset = false;

    public static void init() {
        playerConfig = PlayerConfig.loadFromFile();

        ClientCommands.init();
    }


}
