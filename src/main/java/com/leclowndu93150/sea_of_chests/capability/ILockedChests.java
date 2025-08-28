package com.leclowndu93150.sea_of_chests.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Set;

public interface ILockedChests extends INBTSerializable<CompoundTag> {
    boolean isLocked(BlockPos pos);
    void setLocked(BlockPos pos, boolean locked);
    void unlock(BlockPos pos);
    Set<BlockPos> getLockedChests();
    void clearInvalidChests();
    void clearAll();
}