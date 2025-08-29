package com.leclowndu93150.sea_of_chests.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;

import java.util.HashMap;
import java.util.Map;

public class ChunkLockedChestsImpl implements IChunkLockedChests {
    private final Map<BlockPos, Boolean> lockedPositions = new HashMap<>();
    
    @Override
    public boolean isLocked(BlockPos pos) {
        return lockedPositions.getOrDefault(pos, false);
    }
    
    @Override
    public void setLocked(BlockPos pos, boolean locked) {
        if (locked) {
            lockedPositions.put(pos, true);
        } else {
            lockedPositions.remove(pos);
        }
    }
    
    @Override
    public void removeLocked(BlockPos pos) {
        lockedPositions.remove(pos);
    }
    
    @Override
    public Map<BlockPos, Boolean> getLockedPositions() {
        return new HashMap<>(lockedPositions);
    }
    
    @Override
    public void clear() {
        lockedPositions.clear();
    }
    
    @Override
    public int size() {
        return lockedPositions.size();
    }
    
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        System.out.println("SERIALIZING: Saving " + lockedPositions.size() + " locked chests to NBT");
        for (BlockPos pos : lockedPositions.keySet()) {
            list.add(NbtUtils.writeBlockPos(pos));
            System.out.println("SERIALIZING: Saving locked chest at " + pos);
        }
        tag.put("LockedChests", list);
        return tag;
    }
    
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        lockedPositions.clear();
        ListTag list = nbt.getList("LockedChests", 10);
        System.out.println("DESERIALIZING: Loading " + list.size() + " locked chests from NBT");
        for (int i = 0; i < list.size(); i++) {
            BlockPos pos = NbtUtils.readBlockPos(list.getCompound(i));
            lockedPositions.put(pos, true);
            System.out.println("DESERIALIZING: Loaded locked chest at " + pos);
        }
    }
}