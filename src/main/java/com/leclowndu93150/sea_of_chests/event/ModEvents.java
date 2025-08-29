package com.leclowndu93150.sea_of_chests.event;

import com.leclowndu93150.sea_of_chests.SeaOfChests;
import com.leclowndu93150.sea_of_chests.capability.IChunkLockedChests;
import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.capability.IWorldLockedChestHandler;
import com.leclowndu93150.sea_of_chests.capability.WorldLockedChestHandlerProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SeaOfChests.MODID)
public class ModEvents {
    
    @SubscribeEvent
    public static void onAttachCapabilitiesLevel(AttachCapabilitiesEvent<Level> event) {
        event.addCapability(new ResourceLocation(SeaOfChests.MODID, "world_locked_chest_handler"), new WorldLockedChestHandlerProvider());
    }
    
    @SubscribeEvent
    public static void onAttachCapabilitiesChunk(AttachCapabilitiesEvent<LevelChunk> event) {
        event.addCapability(new ResourceLocation(SeaOfChests.MODID, "chunk_locked_chests"), new ChunkLockedChestsProvider());
    }
    
    @Mod.EventBusSubscriber(modid = SeaOfChests.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBus {
        
        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(IChunkLockedChests.class);
            event.register(IWorldLockedChestHandler.class);
        }
    }
}