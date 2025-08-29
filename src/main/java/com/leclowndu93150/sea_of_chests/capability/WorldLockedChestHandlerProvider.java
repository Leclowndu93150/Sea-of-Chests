package com.leclowndu93150.sea_of_chests.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldLockedChestHandlerProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    public static Capability<IWorldLockedChestHandler> WORLD_LOCKED_CHEST_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    
    private IWorldLockedChestHandler handler = null;
    private final LazyOptional<IWorldLockedChestHandler> optional = LazyOptional.of(this::createHandler);
    
    private IWorldLockedChestHandler createHandler() {
        if (this.handler == null) {
            this.handler = new WorldLockedChestHandlerImpl();
        }
        return this.handler;
    }
    
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == WORLD_LOCKED_CHEST_HANDLER_CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }
    
    @Override
    public CompoundTag serializeNBT() {
        return createHandler().serializeNBT();
    }
    
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createHandler().deserializeNBT(nbt);
    }
}