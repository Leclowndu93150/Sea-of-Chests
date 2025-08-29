package com.leclowndu93150.sea_of_chests.mixin;

import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.capability.WorldLockedChestHandlerProvider;
import com.leclowndu93150.sea_of_chests.network.ModNetworking;
import com.leclowndu93150.sea_of_chests.network.SyncChunkLockedChestsPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ChunkMap.class)
public class ChunkManagerMixin {
    
    @Inject(at = @At("TAIL"), method = "playerLoadedChunk")
    private void playerLoadedChunk(ServerPlayer player, MutableObject<ClientboundLevelChunkWithLightPacket> pkts, LevelChunk chunk, CallbackInfo ci) {
        System.out.println("SERVER: Player " + player.getName().getString() + " loaded chunk " + chunk.getPos());
        
        // Check chunk capability directly - this is where the data should be
        chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
            var chests = chunkLockedChests.getLockedPositions();
            System.out.println("SERVER: Chunk " + chunk.getPos() + " has " + chests.size() + " locked chests in capability");
            if (!chests.isEmpty()) {
                Set<BlockPos> positions = chests.keySet();
                System.out.println("SERVER: Syncing " + positions.size() + " locked chests to player");
                ModNetworking.sendToPlayer(new SyncChunkLockedChestsPacket(chunk.getPos(), positions), player);
            }
        });
    }
}