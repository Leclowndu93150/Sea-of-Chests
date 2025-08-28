package com.leclowndu93150.sea_of_chests.init;

import com.leclowndu93150.sea_of_chests.SeaOfChests;
import com.leclowndu93150.sea_of_chests.block.UnlockingStationBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SeaOfChests.MODID);
    
    public static final RegistryObject<Block> UNLOCKING_STATION = BLOCKS.register("unlocking_station",
            () -> new UnlockingStationBlock(BlockBehaviour.Properties.copy(Blocks.ENCHANTING_TABLE)
                    .strength(5.0F, 1200.0F)
                    .requiresCorrectToolForDrops()));
    
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}