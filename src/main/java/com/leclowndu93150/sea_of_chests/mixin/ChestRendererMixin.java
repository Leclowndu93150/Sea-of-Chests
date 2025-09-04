package com.leclowndu93150.sea_of_chests.mixin;

import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.client.renderer.LockRenderer;
import com.leclowndu93150.sea_of_chests.init.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestRenderer.class)
public class ChestRendererMixin<T extends BlockEntity & LidBlockEntity> {
    private static final ItemStack LOCK_ITEM_STACK = new ItemStack(ModItems.LOCK.get());
    
    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("TAIL"))
    private void renderLock(T blockEntity, float partialTick, PoseStack poseStack, 
                           MultiBufferSource bufferSource, int light, int overlay, CallbackInfo ci) {
        Level level = blockEntity.getLevel();
        if (level == null || !level.isClientSide()) return;

        LevelChunk chunk = level.getChunkAt(blockEntity.getBlockPos());
        boolean isLocked = chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY)
            .map(cap -> cap.getLockedPositions().containsKey(blockEntity.getBlockPos()))
            .orElse(false);
            
        if (!isLocked) return;

        poseStack.pushPose();

        BlockState state = blockEntity.getBlockState();
        if (state.getBlock() instanceof ChestBlock) {
            positionOnChest(poseStack, state);
        }
        
        poseStack.translate(LockRenderer.LOCK_X_OFFSET, LockRenderer.LOCK_Y_OFFSET, 0);
        poseStack.scale(LockRenderer.LOCK_SCALE, LockRenderer.LOCK_SCALE, LockRenderer.LOCK_SCALE);
        
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(LOCK_ITEM_STACK, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, level, 0);
        
        poseStack.popPose();
    }
    
    private static void positionOnChest(PoseStack poseStack, BlockState state) {
        Direction facing = state.getValue(ChestBlock.FACING);
        ChestType type = state.getValue(ChestBlock.TYPE);

        poseStack.translate(0.5, 0.5, 0.5);

        float rotation = switch (facing) {
            case NORTH -> 0;
            case SOUTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        if (type == ChestType.LEFT) {
            poseStack.translate(-0.25, 0, 0);
        } else if (type == ChestType.RIGHT) {
            poseStack.translate(0.25, 0, 0);
        }

        poseStack.translate(0, 0, -0.4875);
    }
}