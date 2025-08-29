package com.leclowndu93150.sea_of_chests.item;

import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.util.ChestUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public class MagnifyingGlassItem extends Item {
    
    public MagnifyingGlassItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        
        if (level.isClientSide() || player == null) {
            return InteractionResult.SUCCESS;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RandomizableContainerBlockEntity container) {
            if (container instanceof ChestBlockEntity || container instanceof BarrelBlockEntity) {
                LevelChunk chunk = level.getChunkAt(pos);
                
                chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                    if (chunkLockedChests.isLocked(pos)) {
                        ChestUtils.showPeekScreen(player, container, pos);
                    } else {
                        player.displayClientMessage(
                            Component.literal("This chest is not locked.").withStyle(style -> style.withColor(0x55FF55)), 
                            true
                        );
                    }
                });
                
                return InteractionResult.SUCCESS;
            }
        }
        
        return InteractionResult.PASS;
    }
}