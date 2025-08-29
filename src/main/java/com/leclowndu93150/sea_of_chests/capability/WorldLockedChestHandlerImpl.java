package com.leclowndu93150.sea_of_chests.capability;

import com.leclowndu93150.sea_of_chests.network.ModNetworking;
import com.leclowndu93150.sea_of_chests.network.SyncChunkLockedChestsPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorldLockedChestHandlerImpl implements IWorldLockedChestHandler {
    private final Map<ChunkPos, Set<BlockPos>> loadedChests = new ConcurrentHashMap<>();
    
    @Override
    public Map<ChunkPos, Set<BlockPos>> getLoadedChests() {
        return new HashMap<>(loadedChests);
    }
    
    @Override
    public void addChest(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        loadedChests.computeIfAbsent(chunkPos, k -> ConcurrentHashMap.newKeySet()).add(pos);
    }
    
    @Override
    public void removeChest(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos> chests = loadedChests.get(chunkPos);
        if (chests != null) {
            chests.remove(pos);
            if (chests.isEmpty()) {
                loadedChests.remove(chunkPos);
            }
        }
    }
    
    @Override
    public boolean isLocked(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos> chests = loadedChests.get(chunkPos);
        return chests != null && chests.contains(pos);
    }
    
    @Override
    public void setLocked(BlockPos pos, boolean locked) {
        if (locked) {
            addChest(pos);
        } else {
            removeChest(pos);
        }
    }
    
    @Override
    public void onChunkLoad(LevelChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
            Set<BlockPos> positions = new HashSet<>();
            for (BlockPos pos : chunkLockedChests.getLockedPositions().keySet()) {
                positions.add(pos);
            }
            if (!positions.isEmpty()) {
                loadedChests.put(chunkPos, ConcurrentHashMap.newKeySet(positions.size()));
                loadedChests.get(chunkPos).addAll(positions);
            }
        });
    }
    
    @Override
    public void onChunkUnload(LevelChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        loadedChests.remove(chunkPos);
    }
    
    @Override
    public Set<BlockPos> getChestsInChunk(ChunkPos chunkPos) {
        return loadedChests.getOrDefault(chunkPos, Collections.emptySet());
    }
    
    @Override
    public void syncChunkToPlayers(LevelChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        Set<BlockPos> chests = getChestsInChunk(chunkPos);
        if (!chests.isEmpty()) {
            SyncChunkLockedChestsPacket packet = new SyncChunkLockedChestsPacket(chunkPos, chests);
            
            for (ServerPlayer player : chunk.getLevel().getServer().getPlayerList().getPlayers()) {
                if (player.level() == chunk.getLevel() && player.distanceToSqr(
                    (chunkPos.x << 4) + 8.0, player.getY(), (chunkPos.z << 4) + 8.0) < 12800) { // 8 chunk radius
                    ModNetworking.sendToPlayer(packet, player);
                }
            }
        }
    }
    
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag chunksList = new ListTag();
        
        for (Map.Entry<ChunkPos, Set<BlockPos>> entry : loadedChests.entrySet()) {
            CompoundTag chunkTag = new CompoundTag();
            chunkTag.putInt("ChunkX", entry.getKey().x);
            chunkTag.putInt("ChunkZ", entry.getKey().z);
            
            ListTag positionsList = new ListTag();
            for (BlockPos pos : entry.getValue()) {
                positionsList.add(NbtUtils.writeBlockPos(pos));
            }
            chunkTag.put("Positions", positionsList);
            chunksList.add(chunkTag);
        }
        
        tag.put("LoadedChests", chunksList);
        return tag;
    }
    
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        loadedChests.clear();
        ListTag chunksList = nbt.getList("LoadedChests", 10);
        
        for (int i = 0; i < chunksList.size(); i++) {
            CompoundTag chunkTag = chunksList.getCompound(i);
            ChunkPos chunkPos = new ChunkPos(chunkTag.getInt("ChunkX"), chunkTag.getInt("ChunkZ"));
            
            Set<BlockPos> positions = ConcurrentHashMap.newKeySet();
            ListTag positionsList = chunkTag.getList("Positions", 10);
            for (int j = 0; j < positionsList.size(); j++) {
                positions.add(NbtUtils.readBlockPos(positionsList.getCompound(j)));
            }
            
            loadedChests.put(chunkPos, positions);
        }
    }
}