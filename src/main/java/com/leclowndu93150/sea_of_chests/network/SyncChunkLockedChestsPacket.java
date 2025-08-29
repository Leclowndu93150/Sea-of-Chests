package com.leclowndu93150.sea_of_chests.network;

import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class SyncChunkLockedChestsPacket {
    private final ChunkPos chunkPos;
    private final Set<BlockPos> lockedPositions;
    
    public SyncChunkLockedChestsPacket(ChunkPos chunkPos, Set<BlockPos> lockedPositions) {
        this.chunkPos = chunkPos;
        this.lockedPositions = lockedPositions;
    }
    
    public SyncChunkLockedChestsPacket(LevelChunk chunk, Set<BlockPos> lockedPositions) {
        this(chunk.getPos(), lockedPositions);
    }
    
    public SyncChunkLockedChestsPacket(FriendlyByteBuf buf) {
        this.chunkPos = new ChunkPos(buf.readInt(), buf.readInt());
        int size = buf.readInt();
        this.lockedPositions = new HashSet<>();
        for (int i = 0; i < size; i++) {
            this.lockedPositions.add(buf.readBlockPos());
        }
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(chunkPos.x);
        buf.writeInt(chunkPos.z);
        buf.writeInt(lockedPositions.size());
        for (BlockPos pos : lockedPositions) {
            buf.writeBlockPos(pos);
        }
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.level.hasChunk(chunkPos.x, chunkPos.z)) {
                LevelChunk chunk = mc.level.getChunk(chunkPos.x, chunkPos.z);
                chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                    chunkLockedChests.clear();
                    for (BlockPos pos : lockedPositions) {
                        chunkLockedChests.setLocked(pos, true);
                    }
                });
            }
        });
        return true;
    }
}