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
        super(ModMenuTypes.PEEK_CHEST_MENU.get(), windowId, playerInventory, container, container.getContainerSize() / 9);
        
        this.slots.clear();
        
        int rows = container.getContainerSize() / 9;
        int containerInventoryStart = (rows - 4) * 18;
        
        for(int row = 0; row < rows; ++row) {
            for(int col = 0; col < 9; ++col) {
                this.addSlot(new PeekSlot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
        
        for(int row = 0; row < 3; ++row) {
            for(int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + containerInventoryStart));
            }
        }
        
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