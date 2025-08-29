package com.leclowndu93150.sea_of_chests.util;

import com.leclowndu93150.sea_of_chests.init.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PeekChestMenu extends ChestMenu {
    
    public PeekChestMenu(int windowId, Inventory playerInventory) {
        this(windowId, playerInventory, new SimpleContainer(27));
    }
    
    public PeekChestMenu(int windowId, Inventory playerInventory, Container container) {
        super(ModMenuTypes.PEEK_CHEST_MENU.get(), windowId, playerInventory, new SimpleContainer(0), 0);

        int containerSize = Math.max(container.getContainerSize(), 1);
        int rows = Math.max(1, (containerSize + 8) / 9);
        int containerInventoryStart = (rows - 4) * 18;
        
        // Add container slots
        for(int row = 0; row < rows; ++row) {
            for(int col = 0; col < 9; ++col) {
                int slotIndex = col + row * 9;
                if (slotIndex < containerSize) {
                    this.addSlot(new PeekSlot(container, slotIndex, 8 + col * 18, 18 + row * 18));
                }
            }
        }
        
        // Add player inventory slots (27 slots: 9-35)
        for(int row = 0; row < 3; ++row) {
            for(int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + containerInventoryStart));
            }
        }
        
        // Add player hotbar slots (9 slots: 0-8)
        for(int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 161 + containerInventoryStart));
        }
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    
    public int getRowCount() {
        return this.container.getContainerSize() / 9;
    }
    
    static class PeekSlot extends Slot {
        public PeekSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }
        
        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
        
        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}