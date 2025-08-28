package com.leclowndu93150.sea_of_chests.event;

import com.leclowndu93150.sea_of_chests.SeaOfChests;
import com.leclowndu93150.sea_of_chests.capability.LockedChestsProvider;
import com.leclowndu93150.sea_of_chests.network.ModNetworking;
import com.leclowndu93150.sea_of_chests.network.UpdateLockStatePacket;
import com.leclowndu93150.sea_of_chests.util.ChestUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SeaOfChests.MODID)
public class ChestLockingEvents {
    
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        
        if (level.isClientSide()) return;
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RandomizableContainerBlockEntity container) {
            if (container instanceof ChestBlockEntity || container instanceof BarrelBlockEntity) {
                level.getCapability(LockedChestsProvider.LOCKED_CHESTS_CAPABILITY).ifPresent(lockedChests -> {
                    boolean isLootChest = container.lootTable != null;
                    
                    if (isLootChest && !lockedChests.isLocked(pos)) {
                        lockedChests.setLocked(pos, true);
                        // Sync to all players
                        ModNetworking.sendToClients(new UpdateLockStatePacket(pos, true));
                        if (!level.isClientSide && container.lootTable != null) {
                            container.unpackLootTable(player);
                        }
                    }
                    
                    if (lockedChests.isLocked(pos)) {
                        event.setCanceled(true);
                        
                        if (!player.isShiftKeyDown()) {
                            player.displayClientMessage(
                                Component.literal("This chest is locked! Bring it to an Unlocking Station.").withStyle(style -> style.withColor(0xFF5555)), 
                                true
                            );
                        } else {
                            ChestUtils.showPeekScreen(player, container, pos);
                        }
                    }
                });
            }
        }
    }
}