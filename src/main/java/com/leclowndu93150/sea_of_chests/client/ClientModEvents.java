package com.leclowndu93150.sea_of_chests.client;

import com.leclowndu93150.sea_of_chests.SeaOfChests;
import com.leclowndu93150.sea_of_chests.client.screen.PeekChestScreen;
import com.leclowndu93150.sea_of_chests.client.screen.UnlockingStationScreen;
import com.leclowndu93150.sea_of_chests.init.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SeaOfChests.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.UNLOCKING_STATION_MENU.get(), UnlockingStationScreen::new);
            MenuScreens.register(ModMenuTypes.PEEK_CHEST_MENU.get(), PeekChestScreen::new);
        });
    }
}