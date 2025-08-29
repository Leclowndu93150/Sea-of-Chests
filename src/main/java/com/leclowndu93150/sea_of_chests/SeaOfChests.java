package com.leclowndu93150.sea_of_chests;

import com.leclowndu93150.sea_of_chests.init.*;
import com.leclowndu93150.sea_of_chests.network.ModNetworking;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SeaOfChests.MODID)
public class SeaOfChests {

    public static final String MODID = "sea_of_chests";
    static final Logger LOGGER = LogUtils.getLogger();

    public SeaOfChests() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        ModCreativeTabs.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        
        modEventBus.addListener(this::commonSetup);
        
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetworking.register();
        });
    }

}
