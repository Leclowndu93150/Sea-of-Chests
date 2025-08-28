package com.leclowndu93150.sea_of_chests.init;

import com.leclowndu93150.sea_of_chests.SeaOfChests;
import com.leclowndu93150.sea_of_chests.menu.UnlockingStationMenu;
import com.leclowndu93150.sea_of_chests.util.PeekChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = 
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, SeaOfChests.MODID);
    
    public static final RegistryObject<MenuType<UnlockingStationMenu>> UNLOCKING_STATION_MENU = 
            MENUS.register("unlocking_station_menu",
                    () -> IForgeMenuType.create(UnlockingStationMenu::new));
    
    public static final RegistryObject<MenuType<PeekChestMenu>> PEEK_CHEST_MENU = 
            MENUS.register("peek_chest_menu",
                    () -> new MenuType<>(PeekChestMenu::new, null));
    
    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}