package com.leclowndu93150.sea_of_chests.mixin;

import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseContainerBlockEntity.class)
public class BaseContainerBlockEntityMixin {
    
    @Inject(at = @At("HEAD"), method = "getCapability", cancellable = true, remap = false)
    private void getCapability(Capability cap, Direction side, CallbackInfoReturnable<LazyOptional> cir) {
        BlockEntity te = (BlockEntity) (Object) this;
        if (!te.isRemoved() && cap == ForgeCapabilities.ITEM_HANDLER && te.hasLevel()) {
            var chunk = te.getLevel().getChunkAt(te.getBlockPos());
            chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                if (chunkLockedChests.isLocked(te.getBlockPos())) {
                    cir.setReturnValue(LazyOptional.empty());
                }
            });
        }
    }
}