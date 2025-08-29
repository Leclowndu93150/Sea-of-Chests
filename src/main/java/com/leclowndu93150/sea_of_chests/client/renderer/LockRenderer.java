package com.leclowndu93150.sea_of_chests.client.renderer;

import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.init.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LockRenderer {
    private static final ItemStack LOCK_ITEM_STACK = new ItemStack(ModItems.LOCK.get());
    
    public static void renderLocks(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null || !level.isClientSide()) return;
        
        if (mc.player == null) return;
        
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        Frustum frustum = new Frustum(poseStack.last().pose(), mc.gameRenderer.getProjectionMatrix(mc.options.fov().get()));
        frustum.prepare(cameraPos.x, cameraPos.y, cameraPos.z);
        
        BlockPos playerPos = mc.player.blockPosition();
        final int RENDER_DISTANCE = mc.options.renderDistance().get() * 16;
        final double MAX_DISTANCE_SQ = RENDER_DISTANCE * RENDER_DISTANCE;
        
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

        int chunkX = playerPos.getX() >> 4;
        int chunkZ = playerPos.getZ() >> 4;
        int chunkRadius = (RENDER_DISTANCE >> 4) + 1;
        
        for (int x = chunkX - chunkRadius; x <= chunkX + chunkRadius; x++) {
            for (int z = chunkZ - chunkRadius; z <= chunkZ + chunkRadius; z++) {
                if (level.hasChunk(x, z)) {
                    LevelChunk chunk = level.getChunk(x, z);
                    chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                        for (BlockPos pos : chunkLockedChests.getLockedPositions().keySet()) {
                            double distanceSq = pos.distToCenterSqr(cameraPos.x, cameraPos.y, cameraPos.z);
                            if (distanceSq > MAX_DISTANCE_SQ) continue;

                            AABB lockBB = new AABB(pos.getX(), pos.getY(), pos.getZ(), 
                                                 pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0);
                            if (!frustum.isVisible(lockBB)) continue;
                            
                            if (level.isLoaded(pos)) {
                                var blockEntity = level.getBlockEntity(pos);
                                
                                if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof BarrelBlockEntity) {
                                    poseStack.pushPose();

                                    poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);

                                    BlockState state = level.getBlockState(pos);
                                    if (blockEntity instanceof ChestBlockEntity) {
                                        positionOnChest(poseStack, state);
                                    } else {
                                        positionOnBarrel(poseStack, state);
                                    }

                                    float distance = (float) Math.sqrt(distanceSq);
                                    float scale = Math.min(0.6f, 8.0f / distance);
                                    poseStack.scale(scale, scale, scale);

                                    int light = LevelRenderer.getLightColor(level, mut.set(pos.getX(), pos.getY(), pos.getZ()));

                                    mc.getItemRenderer().renderStatic(LOCK_ITEM_STACK, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, level, 0);
                                    poseStack.popPose();
                                }
                            }
                        }
                    });
                }
            }
        }
        
        bufferSource.endBatch();
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
    
    private static void positionOnBarrel(PoseStack poseStack, BlockState state) {
        Direction facing = state.getValue(BarrelBlock.FACING);

        poseStack.translate(0.5, 0.5, 0.5);

        float rotationY = switch (facing) {
            case NORTH -> 0;
            case SOUTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        };
        
        float rotationX = switch (facing) {
            case UP -> -90;
            case DOWN -> 90;
            default -> 0;
        };
        
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationY));
        poseStack.mulPose(Axis.XP.rotationDegrees(rotationX));

        poseStack.translate(0, 0, -0.4875);
    }
}