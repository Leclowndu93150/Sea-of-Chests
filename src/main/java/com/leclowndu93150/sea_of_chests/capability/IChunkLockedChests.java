package com.leclowndu93150.sea_of_chests.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Map;

public interface IChunkLockedChests extends INBTSerializable<CompoundTag> {
    boolean isLocked(BlockPos pos);
    void setLocked(BlockPos pos, boolean locked);
    void removeLocked(BlockPos pos);
    Map<BlockPos, Boolean> getLockedPositions();
    void clear();
    int size();
}