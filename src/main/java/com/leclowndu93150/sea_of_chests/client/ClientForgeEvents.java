package com.leclowndu93150.sea_of_chests.client;

import com.leclowndu93150.sea_of_chests.SeaOfChests;
import com.leclowndu93150.sea_of_chests.client.cache.LockedChestRenderCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SeaOfChests.MODID, value = Dist.CLIENT)
public class ClientForgeEvents {
    
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide() && event.getChunk() != null) {
            ChunkPos chunkPos = event.getChunk().getPos();
            ClientCacheHandler.onChunkLoaded(chunkPos);
        }
    }
    
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel().isClientSide() && event.getChunk() != null) {
            ChunkPos chunkPos = event.getChunk().getPos();
            ClientCacheHandler.onChunkUnloaded(chunkPos);
        }
    }
    
    @SubscribeEvent
    public static void onClientLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientCacheHandler.onWorldChange();
    }
    
    @SubscribeEvent
    public static void onClientLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientCacheHandler.onWorldChange();
    }
}