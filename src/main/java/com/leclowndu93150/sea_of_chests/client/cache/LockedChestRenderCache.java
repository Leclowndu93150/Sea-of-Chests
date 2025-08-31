package com.leclowndu93150.sea_of_chests.client.cache;

import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LockedChestRenderCache {
    private static final Map<ChunkPos, ChunkCache> chunkCaches = new ConcurrentHashMap<>();
    private static final Long2ObjectOpenHashMap<List<CachedLock>> frustumCache = new Long2ObjectOpenHashMap<>();
    private static long lastFrustumCacheFrame = -1;
    
    public static class CachedLock {
        public final BlockPos pos;
        public final BlockState state;
        public final AABB boundingBox;
        public final boolean isChest;
        public final boolean isBarrel;
        
        public CachedLock(BlockPos pos, BlockState state, boolean isChest, boolean isBarrel) {
            this.pos = pos;
            this.state = state;
            this.isChest = isChest;
            this.isBarrel = isBarrel;
            this.boundingBox = new AABB(pos.getX(), pos.getY(), pos.getZ(), 
                                       pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0);
        }
    }
    
    public static class ChunkCache {
        private final List<CachedLock> locks = new ObjectArrayList<>();
        private boolean needsUpdate = true;
        
        public void addLock(CachedLock lock) {
            locks.add(lock);
        }
        
        public void clearLocks() {
            locks.clear();
            needsUpdate = true;
        }
        
        public List<CachedLock> getLocks() {
            return locks;
        }
        
        public void removeLock(CachedLock lock) {
            locks.remove(lock);
        }
        
        public void markDirty() {
            needsUpdate = true;
        }
        
        public boolean needsUpdate() {
            return needsUpdate;
        }
        
        public void clearUpdateFlag() {
            needsUpdate = false;
        }
    }
    
    public static ChunkCache getOrCreateChunkCache(ChunkPos chunkPos) {
        return chunkCaches.computeIfAbsent(chunkPos, k -> new ChunkCache());
    }
    
    public static ChunkCache getChunkCache(ChunkPos chunkPos) {
        return chunkCaches.get(chunkPos);
    }
    
    public static void removeChunkCache(ChunkPos chunkPos) {
        chunkCaches.remove(chunkPos);
    }
    
    public static void markChunkDirty(ChunkPos chunkPos) {
        ChunkCache cache = chunkCaches.get(chunkPos);
        if (cache != null) {
            cache.markDirty();
        }
    }
    
    public static void clearAll() {
        chunkCaches.clear();
        frustumCache.clear();
    }
    
    public static void invalidateFrustumCache() {
        frustumCache.clear();
    }
    
    public static List<CachedLock> getFrustumCached(long frameCount, ChunkPos chunkPos) {
        if (frameCount != lastFrustumCacheFrame) {
            frustumCache.clear();
            lastFrustumCacheFrame = frameCount;
        }
        return frustumCache.get(chunkPos.toLong());
    }
    
    public static void putFrustumCached(ChunkPos chunkPos, List<CachedLock> locks) {
        frustumCache.put(chunkPos.toLong(), locks);
    }
    
    public static void validateCache(Level level, ChunkPos chunkPos) {
        ChunkCache cache = chunkCaches.get(chunkPos);
        if (cache == null || cache.getLocks().isEmpty()) {
            return;
        }
        
        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
        chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
            Iterator<CachedLock> iterator = cache.getLocks().iterator();
            boolean changed = false;
            
            while (iterator.hasNext()) {
                CachedLock lock = iterator.next();
                
                if (level.isLoaded(lock.pos)) {
                    BlockEntity blockEntity = level.getBlockEntity(lock.pos);
                    boolean shouldRemove = false;
                    
                    if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof BarrelBlockEntity) {
                        if (!chunkLockedChests.isLocked(lock.pos)) {
                            shouldRemove = true;
                        }
                    } else {
                        shouldRemove = true;
                    }
                    
                    if (shouldRemove) {
                        iterator.remove();
                        changed = true;
                    }
                }
            }
            
            if (changed) {
                invalidateFrustumCache();
            }
        });
    }
    
    public static void validateAllCaches(Level level) {
        for (ChunkPos chunkPos : chunkCaches.keySet()) {
            if (level.hasChunk(chunkPos.x, chunkPos.z)) {
                validateCache(level, chunkPos);
            }
        }
    }
}