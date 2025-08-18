package com.beatcraft.client;

import com.beatcraft.Beatcraft;
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

    public static void earlyInit() {
        Beatcraft.LOGGER.info("Initializing Beatcraft Neoforge");
        playerConfig = PlayerConfig.loadFromFile();

    }

    public static void initCommands() {
        Beatcraft.LOGGER.info("Initializing commands");
        ClientCommands.init();
    }


}
