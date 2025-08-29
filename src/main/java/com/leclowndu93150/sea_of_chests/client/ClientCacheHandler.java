package com.leclowndu93150.sea_of_chests.client;

import com.leclowndu93150.sea_of_chests.client.cache.LockedChestRenderCache;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientCacheHandler {
    
    public static void onLockStateChanged(BlockPos pos, boolean locked) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            ChunkPos chunkPos = new ChunkPos(pos);
            LockedChestRenderCache.markChunkDirty(chunkPos);
            LockedChestRenderCache.invalidateFrustumCache();
        }
    }
    
    public static void onChunkLoaded(ChunkPos chunkPos) {
        LockedChestRenderCache.markChunkDirty(chunkPos);
    }
    
    public static void onChunkUnloaded(ChunkPos chunkPos) {
        LockedChestRenderCache.removeChunkCache(chunkPos);
        LockedChestRenderCache.invalidateFrustumCache();
    }
    
    public static void onWorldChange() {
        LockedChestRenderCache.clearAll();
    }
    
    public static void onBlockUpdate(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        LockedChestRenderCache.markChunkDirty(chunkPos);
        LockedChestRenderCache.invalidateFrustumCache();
    }
}