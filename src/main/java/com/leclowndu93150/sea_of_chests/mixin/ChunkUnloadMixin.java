package com.leclowndu93150.sea_of_chests.mixin;

import com.leclowndu93150.sea_of_chests.capability.WorldLockedChestHandlerProvider;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkMap.class)
public class ChunkUnloadMixin {
    
    @Shadow
    @Final
    ServerLevel level;
    
    @Inject(at = @At("HEAD"), method = "save(Lnet/minecraft/world/level/chunk/ChunkAccess;)Z")
    private void onChunkSave(ChunkAccess p_140259_, CallbackInfoReturnable<Boolean> cir) {
        if (p_140259_ instanceof LevelChunk levelChunk) {
            level.getCapability(WorldLockedChestHandlerProvider.WORLD_LOCKED_CHEST_HANDLER_CAPABILITY).ifPresent(handler -> {
                handler.onChunkUnload(levelChunk);
            });
        }
    }
}