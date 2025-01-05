package com.beatcraft;

import com.beatcraft.data.components.ModComponents;
import com.beatcraft.items.ModItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeatCraft implements ModInitializer {
	public static final String MOD_ID = "beatcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("hi :3");

		ModComponents.init();
		ModItems.init();

	}
}