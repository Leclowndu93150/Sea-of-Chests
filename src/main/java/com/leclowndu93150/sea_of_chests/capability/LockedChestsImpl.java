package com.leclowndu93150.sea_of_chests.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;

import java.util.HashSet;
import java.util.Set;

public class LockedChestsImpl implements ILockedChests {
    private final Set<BlockPos> lockedChests = new HashSet<>();
    
    @Override
    public boolean isLocked(BlockPos pos) {
        return lockedChests.contains(pos);
    }
    
    @Override
    public void setLocked(BlockPos pos, boolean locked) {
        if (locked) {
            lockedChests.add(pos);
        } else {
            lockedChests.remove(pos);
        }
    }
    
    @Override
    public void unlock(BlockPos pos) {
        lockedChests.remove(pos);
    }
    
    @Override
    public Set<BlockPos> getLockedChests() {
        return new HashSet<>(lockedChests);
    }
    
    @Override
    public void clearInvalidChests() {
        lockedChests.clear();
    }
    
    @Override
    public void clearAll() {
        lockedChests.clear();
    }
    
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (BlockPos pos : lockedChests) {
            list.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("LockedChests", list);
        return tag;
    }
    
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        lockedChests.clear();
        ListTag list = nbt.getList("LockedChests", 10);
        for (int i = 0; i < list.size(); i++) {
            lockedChests.add(NbtUtils.readBlockPos(list.getCompound(i)));
        }
    }
}