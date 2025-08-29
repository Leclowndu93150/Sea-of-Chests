package com.leclowndu93150.sea_of_chests.command;

import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.capability.WorldLockedChestHandlerProvider;
import com.leclowndu93150.sea_of_chests.client.ClientCacheHandler;
import com.leclowndu93150.sea_of_chests.network.ModNetworking;
import com.leclowndu93150.sea_of_chests.network.UpdateLockStatePacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class DebugCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("seaofchests")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawn_locked_chest")
                        .executes(DebugCommand::spawnLockedChest)
                )
        );
    }
    
    private static int spawnLockedChest(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        HitResult hitResult = player.pick(10.0, 0.0F, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            source.sendFailure(Component.literal("You must be looking at a block"));
            return 0;
        }
        
        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        BlockPos targetPos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
        ServerLevel level = player.serverLevel();
        
        BlockState currentState = level.getBlockState(targetPos);
        if (!currentState.isAir()) {
            source.sendFailure(Component.literal("Target position is not air"));
            return 0;
        }
        
        BlockState chestState = Blocks.CHEST.defaultBlockState();
        level.setBlock(targetPos, chestState, 3);
        
        if (level.getBlockEntity(targetPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON, level.random.nextLong());
            
            LevelChunk chunk = level.getChunkAt(targetPos);
            chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                chunkLockedChests.setLocked(targetPos, true);
            });
            
            level.getCapability(WorldLockedChestHandlerProvider.WORLD_LOCKED_CHEST_HANDLER_CAPABILITY).ifPresent(worldHandler -> {
                worldHandler.addChest(targetPos);
            });
            
            ModNetworking.sendToClients(new UpdateLockStatePacket(targetPos, true));

            ClientCacheHandler.onLockStateChanged(targetPos, true);
            
            source.sendSuccess(() -> Component.literal("Spawned locked loot chest at " + targetPos), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("Failed to create chest block entity"));
            return 0;
        }
    }
}