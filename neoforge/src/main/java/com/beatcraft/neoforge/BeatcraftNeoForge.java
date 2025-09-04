package com.beatcraft.neoforge;

import com.beatcraft.Beatcraft;
import com.beatcraft.neoforge.common.items.NeoforgeItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(Beatcraft.MOD_ID)
public final class BeatcraftNeoForge {
    public BeatcraftNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Run our common setup.
        Beatcraft.init();

        modEventBus.addListener(this::commonSetup);

        // NeoForge.EVENT_BUS.register(this);

        NeoforgeItems.register(modEventBus);

        modEventBus.addListener(this::addCreativeTab);

    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private void addCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(NeoforgeItems.SABER_ITEM);
        }
    }



}
