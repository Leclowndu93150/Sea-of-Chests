package com.leclowndu93150.sea_of_chests.mixin;

import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import com.leclowndu93150.sea_of_chests.ChestLockingTicker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public class MixinLevelChunk {
    @Inject(method = "updateBlockEntityTicker", at = @At(value = "HEAD"))
    private void seaOfChestsUpdateBlockEntityTicker(BlockEntity entity, CallbackInfo cir) {
        if (entity instanceof RandomizableContainerBlockEntity incoming) {
            if (incoming instanceof ChestBlockEntity || incoming instanceof BarrelBlockEntity) {
                LevelChunk chunk = (LevelChunk) (Object) this;
                if (chunk.getLevel().isClientSide()) {
                    return;
                }
                if (incoming.lootTable != null) {
                    ChestLockingTicker.addEntry(incoming, chunk.getLevel(), entity.getBlockPos());
                }
            }
        }
    }
}