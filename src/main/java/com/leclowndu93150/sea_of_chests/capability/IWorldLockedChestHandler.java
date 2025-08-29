package com.leclowndu93150.sea_of_chests.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Map;
import java.util.Set;

public interface IWorldLockedChestHandler extends INBTSerializable<CompoundTag> {
    Map<ChunkPos, Set<BlockPos>> getLoadedChests();
    void addChest(BlockPos pos);
    void removeChest(BlockPos pos);
    boolean isLocked(BlockPos pos);
    void setLocked(BlockPos pos, boolean locked);
    void onChunkLoad(LevelChunk chunk);
    void onChunkUnload(LevelChunk chunk);
    Set<BlockPos> getChestsInChunk(ChunkPos chunkPos);
    void syncChunkToPlayers(LevelChunk chunk);
}