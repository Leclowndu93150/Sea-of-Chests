package com.leclowndu93150.sea_of_chests.init;

import com.leclowndu93150.sea_of_chests.SeaOfChests;
import com.leclowndu93150.sea_of_chests.item.LockpickItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SeaOfChests.MODID);
    
    public static final RegistryObject<Item> LOCKPICK = ITEMS.register("lockpick",
            () -> new LockpickItem(new Item.Properties().stacksTo(16)));
    
    public static final RegistryObject<Item> UNLOCKING_STATION = ITEMS.register("unlocking_station",
            () -> new BlockItem(ModBlocks.UNLOCKING_STATION.get(), new Item.Properties()));
    
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}