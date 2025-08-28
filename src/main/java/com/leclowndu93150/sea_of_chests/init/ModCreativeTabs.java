package com.leclowndu93150.sea_of_chests.init;

import com.leclowndu93150.sea_of_chests.SeaOfChests;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SeaOfChests.MODID);
    
    public static final RegistryObject<CreativeModeTab> SEA_OF_CHESTS_TAB = CREATIVE_MODE_TABS.register("sea_of_chests_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.LOCKPICK.get()))
                    .title(Component.translatable("creativetab.sea_of_chests_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.LOCKPICK.get());
                        output.accept(ModItems.UNLOCKING_STATION.get());
                    })
                    .build());
    
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}