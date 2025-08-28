package com.leclowndu93150.sea_of_chests.mixin.client;

import com.leclowndu93150.sea_of_chests.capability.LockedChestsProvider;
import com.leclowndu93150.sea_of_chests.client.renderer.LockRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestRenderer.class)
public class ChestRendererMixin<T extends BlockEntity & LidBlockEntity> {

    @Inject(method = "render", at = @At("TAIL"), remap = false)
    private void renderLock(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, CallbackInfo ci) {
        Level level = blockEntity.getLevel();
        if (level == null || !level.isClientSide()) return;
        
        BlockPos pos = blockEntity.getBlockPos();
        if (pos.equals(BlockPos.ZERO)) return;
        
        level.getCapability(LockedChestsProvider.LOCKED_CHESTS_CAPABILITY).ifPresent(lockedChests -> {
            if (lockedChests.isLocked(pos)) {
                renderLockModel(poseStack, bufferSource, packedLight, packedOverlay);
            }
        });
    }
    
    private void renderLockModel(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        poseStack.translate(0.5, 0.6, 1.01);
        poseStack.scale(0.2f, 0.2f, 0.2f);

       LockRenderer.renderLock(poseStack, bufferSource, packedLight, packedOverlay);
        
        poseStack.popPose();
    }
}