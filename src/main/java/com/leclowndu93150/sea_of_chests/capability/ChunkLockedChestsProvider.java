package com.leclowndu93150.sea_of_chests.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChunkLockedChestsProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    public static Capability<IChunkLockedChests> CHUNK_LOCKED_CHESTS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    
    private IChunkLockedChests lockedChests = null;
    private final LazyOptional<IChunkLockedChests> optional = LazyOptional.of(this::createLockedChests);
    
    private IChunkLockedChests createLockedChests() {
        if (this.lockedChests == null) {
            this.lockedChests = new ChunkLockedChestsImpl();
        }
        return this.lockedChests;
    }
    
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CHUNK_LOCKED_CHESTS_CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }
    
    @Override
    public CompoundTag serializeNBT() {
        return createLockedChests().serializeNBT();
    }
    
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createLockedChests().deserializeNBT(nbt);
    }
}