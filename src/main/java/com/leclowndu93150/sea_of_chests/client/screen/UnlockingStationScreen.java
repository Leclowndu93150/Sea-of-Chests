package com.leclowndu93150.sea_of_chests.client.screen;

import com.leclowndu93150.sea_of_chests.SeaOfChests;
import com.leclowndu93150.sea_of_chests.menu.UnlockingStationMenu;
import com.leclowndu93150.sea_of_chests.network.ModNetworking;
import com.leclowndu93150.sea_of_chests.network.StartUnlockingPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class UnlockingStationScreen extends AbstractContainerScreen<UnlockingStationMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(SeaOfChests.MODID, "textures/gui/unlocking_station.png");
    private Button unlockButton;
    
    public UnlockingStationScreen(UnlockingStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
    
    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 6;
        
        this.unlockButton = Button.builder(
                Component.literal("Start Unlocking"),
                button -> {
                    ModNetworking.sendToServer(new StartUnlockingPacket());
                })
                .bounds(this.leftPos + 50, this.topPos + 60, 76, 20)
                .build();
        
        this.addRenderableWidget(unlockButton);
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        
        renderProgressArrow(guiGraphics, x, y);
    }
    
    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (menu.isCrafting()) {
            guiGraphics.blit(TEXTURE, x + 105, y + 33, 176, 0, menu.getScaledProgress(), 17);
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
        
        unlockButton.active = !menu.isCrafting() && !menu.getSlot(0).getItem().isEmpty();
    }
}