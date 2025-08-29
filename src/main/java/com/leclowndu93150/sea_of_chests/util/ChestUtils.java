package com.leclowndu93150.sea_of_chests.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.network.NetworkHooks;

public class ChestUtils {
    
    public static void showPeekScreen(Player player, RandomizableContainerBlockEntity container, BlockPos pos) {
        if (player instanceof ServerPlayer serverPlayer) {
             if (container.lootTable != null) {
                 container.unpackLootTable(player);
             }
            
            MenuProvider peekProvider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("Locked Chest (Peek Only)");
                }
                
                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
                    return new PeekChestMenu(windowId, playerInventory, container);
                }
            };
            
            NetworkHooks.openScreen(serverPlayer, peekProvider, pos);
        }
    }
}