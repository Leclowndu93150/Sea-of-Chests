package com.leclowndu93150.sea_of_chests.init;

import com.leclowndu93150.sea_of_chests.SeaOfChests;
import com.leclowndu93150.sea_of_chests.block.entity.UnlockingStationBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SeaOfChests.MODID);
    
    public static final RegistryObject<BlockEntityType<UnlockingStationBlockEntity>> UNLOCKING_STATION = 
            BLOCK_ENTITIES.register("unlocking_station",
                    () -> BlockEntityType.Builder.of(UnlockingStationBlockEntity::new, ModBlocks.UNLOCKING_STATION.get()).build(null));
    
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}