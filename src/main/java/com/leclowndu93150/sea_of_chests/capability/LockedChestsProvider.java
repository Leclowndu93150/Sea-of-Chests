package com.leclowndu93150.sea_of_chests.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LockedChestsProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    public static Capability<ILockedChests> LOCKED_CHESTS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    
    private ILockedChests lockedChests = null;
    private final LazyOptional<ILockedChests> optional = LazyOptional.of(this::createLockedChests);
    
    private ILockedChests createLockedChests() {
        if (this.lockedChests == null) {
            this.lockedChests = new LockedChestsImpl();
        }
        return this.lockedChests;
    }
    
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == LOCKED_CHESTS_CAPABILITY) {
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