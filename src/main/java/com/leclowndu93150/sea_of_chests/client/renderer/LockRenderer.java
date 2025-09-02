package com.leclowndu93150.sea_of_chests.client.renderer;

import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.client.cache.LockedChestRenderCache;
import com.leclowndu93150.sea_of_chests.client.cache.LockedChestRenderCache.CachedLock;
import com.leclowndu93150.sea_of_chests.client.cache.LockedChestRenderCache.ChunkCache;
import com.leclowndu93150.sea_of_chests.init.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LockRenderer {
    private static final ItemStack LOCK_ITEM_STACK = new ItemStack(ModItems.LOCK.get());
    private static long frameCount = 0;
    private static long lastValidationTime = 0;
    private static final long VALIDATION_INTERVAL = 2000;

    public static float LOCK_X_OFFSET = 0.0f;
    public static float LOCK_Y_OFFSET = 0.0f;
    public static float LOCK_SCALE = 0.75f;
    
    public static void renderLocks(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTick) {
        return; // Disabled for performance
        /*
        frameCount++;
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null || !level.isClientSide()) return;
        
        if (mc.player == null) return;
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastValidationTime > VALIDATION_INTERVAL) {
            LockedChestRenderCache.validateAllCaches(level);
            lastValidationTime = currentTime;
        }
        
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        Frustum frustum = new Frustum(poseStack.last().pose(), mc.gameRenderer.getProjectionMatrix(mc.options.fov().get()));
        frustum.prepare(cameraPos.x, cameraPos.y, cameraPos.z);
        
        BlockPos playerPos = mc.player.blockPosition();
        final int RENDER_DISTANCE = mc.options.renderDistance().get() * 16;
        final double MAX_DISTANCE_SQ = RENDER_DISTANCE * RENDER_DISTANCE;
        
        int chunkX = playerPos.getX() >> 4;
        int chunkZ = playerPos.getZ() >> 4;
        int chunkRadius = (RENDER_DISTANCE >> 4) + 1;
        
        List<CachedLock> visibleLocks = new ObjectArrayList<>();
        
        for (int x = chunkX - chunkRadius; x <= chunkX + chunkRadius; x++) {
            for (int z = chunkZ - chunkRadius; z <= chunkZ + chunkRadius; z++) {
                ChunkPos chunkPos = new ChunkPos(x, z);
                
                if (!level.hasChunk(x, z)) continue;
                
                List<CachedLock> frustumCached = LockedChestRenderCache.getFrustumCached(frameCount, chunkPos);
                if (frustumCached != null) {
                    visibleLocks.addAll(frustumCached);
                    continue;
                }
                
                ChunkCache cache = LockedChestRenderCache.getChunkCache(chunkPos);
                
                if (cache == null || cache.needsUpdate()) {
                    updateChunkCache(level, chunkPos);
                    cache = LockedChestRenderCache.getChunkCache(chunkPos);
                }
                
                if (cache != null) {
                    List<CachedLock> chunkLocks = new ObjectArrayList<>();
                    for (CachedLock lock : cache.getLocks()) {
                        if (!frustum.isVisible(lock.boundingBox)) continue;
                        
                        double distanceSq = lock.pos.distToCenterSqr(cameraPos.x, cameraPos.y, cameraPos.z);
                        if (distanceSq > MAX_DISTANCE_SQ) continue;
                        
                        chunkLocks.add(lock);
                        visibleLocks.add(lock);
                    }
                    LockedChestRenderCache.putFrustumCached(chunkPos, chunkLocks);
                }
            }
        }
        
        if (!visibleLocks.isEmpty()) {
            for (CachedLock lock : visibleLocks) {
                renderLock(poseStack, bufferSource, level, lock, cameraPos);
            }
            bufferSource.endBatch();
        }
        */
    }
    
    private static void updateChunkCache(Level level, ChunkPos chunkPos) {
        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
        ChunkCache cache = LockedChestRenderCache.getOrCreateChunkCache(chunkPos);
        cache.clearLocks();
        
        chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
            for (BlockPos pos : chunkLockedChests.getLockedPositions().keySet()) {
                if (level.isLoaded(pos)) {
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof BarrelBlockEntity) {
                        BlockState state = level.getBlockState(pos);
                        cache.addLock(new CachedLock(pos, state, 
                            blockEntity instanceof ChestBlockEntity,
                            blockEntity instanceof BarrelBlockEntity));
                    }
                }
            }
        });
        
        cache.clearUpdateFlag();
    }
    
    private static void renderLock(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, 
                                  Level level, CachedLock lock, Vec3 cameraPos) {
        poseStack.pushPose();
        
        double dx = lock.pos.getX() - cameraPos.x;
        double dy = lock.pos.getY() - cameraPos.y;
        double dz = lock.pos.getZ() - cameraPos.z;
        double distanceSq = dx * dx + dy * dy + dz * dz;
        
        poseStack.translate(dx, dy, dz);
        
        if (lock.isChest) {
            positionOnChest(poseStack, lock.state);
        } else if (lock.isBarrel) {
            positionOnBarrel(poseStack, lock.state);
        }
        
        poseStack.translate(LOCK_X_OFFSET, LOCK_Y_OFFSET, 0);
        
        float scale = Math.min(0.6f, 8.0f / (float) Math.sqrt(distanceSq)) * LOCK_SCALE;
        poseStack.scale(scale, scale, scale);
        
        int light = LevelRenderer.getLightColor(level, lock.pos);
        
        Minecraft.getInstance().getItemRenderer().renderStatic(LOCK_ITEM_STACK, 
            ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, 
            poseStack, bufferSource, level, 0);
        
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