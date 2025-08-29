package com.leclowndu93150.sea_of_chests.event;

import com.leclowndu93150.sea_of_chests.SeaOfChests;
import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SeaOfChests.MODID)
public class ChestLockingEvents {
    
    private static final float LOCKED_CHEST_BREAK_SPEED_MODIFIER = 0.1f;
    
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        
        if (level.isClientSide()) return;
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RandomizableContainerBlockEntity container) {
            if (container instanceof ChestBlockEntity || container instanceof BarrelBlockEntity) {
                LevelChunk chunk = level.getChunkAt(pos);

                chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                    if (chunkLockedChests.isLocked(pos)) {
                        ItemStack heldItem = player.getItemInHand(event.getHand());
                        if (heldItem.getItem() == ModItems.MAGNIFYING_GLASS.get() && player.isShiftKeyDown()) {
                            return;
                        }
                        
                        event.setCanceled(true);
                        
                        player.displayClientMessage(
                            Component.literal("This chest is locked! Bring it to an Unlocking Station.").withStyle(style -> style.withColor(0xFF5555)), 
                            true
                        );
                    }
                });
            }
        }
    }
    
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        BlockPos pos = event.getPosition().orElse(null);
        if (pos == null) return;
        
        Level level = event.getEntity().level();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        
        if (blockEntity instanceof RandomizableContainerBlockEntity container) {
            if (container instanceof ChestBlockEntity || container instanceof BarrelBlockEntity) {
                LevelChunk chunk = level.getChunkAt(pos);
                
                chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                    if (chunkLockedChests.isLocked(pos)) {
                        event.setNewSpeed(event.getOriginalSpeed() * LOCKED_CHEST_BREAK_SPEED_MODIFIER);
                    }
                });
            }
        }
    }
}