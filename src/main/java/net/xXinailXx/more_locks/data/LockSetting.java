package net.xXinailXx.more_locks.data;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record LockSetting(Vec3 pos, Quaternion rot) {
    public static IBlockLocking defaultBlock() {
        return new IBlockLocking() {
            public LockSetting getDefaultCustomLockSetting(Level level, BlockPos pos, BlockState state, float partialTick) {
                if (state.getBlock() instanceof TrapDoorBlock) {
                    if (state.getValue(BlockStateProperties.HALF).equals(Half.TOP))
                        return new LockSetting(new Vec3(0.5, 1, 0.5), Vector3f.XP.rotationDegrees(90));
                    else
                        return new LockSetting(new Vec3(0.5, 0.2, 0.5), Vector3f.XP.rotationDegrees(90));
                }

                try {
                    Direction direction = state.getValue(BlockStateProperties.FACING);
                    BlockEntity entity = level.getChunkAt(pos).getBlockEntity(pos);

                    if (entity instanceof BaseContainerBlockEntity)
                        return defaultDynamicSetting(level, state.getBlock().asItem().getDefaultInstance(), partialTick);

                    if (state.getBlock() instanceof FenceGateBlock) {
                        return switch (direction) {
                            case NORTH, UP, DOWN -> new LockSetting(new Vec3(0.5, 0.5, 0.4), Vector3f.YP.rotationDegrees(0));
                            case SOUTH -> new LockSetting(new Vec3(0.5, 0.5, 0.6), Vector3f.YP.rotationDegrees(0));
                            case WEST -> new LockSetting(new Vec3(0.4, 0.5, 0.5), Vector3f.YP.rotationDegrees(90));
                            case EAST -> new LockSetting(new Vec3(0.6, 0.5, 0.5), Vector3f.YP.rotationDegrees(90));
                        };
                    } else {
                        return switch (direction) {
                            case DOWN -> new LockSetting(new Vec3(0.5, 0, 0.5), Vector3f.XP.rotationDegrees(90));
                            case UP -> new LockSetting(new Vec3(0.5, 1, 0.5), Vector3f.XP.rotationDegrees(90));
                            case NORTH -> new LockSetting(new Vec3(0.5, 0.5, 0), Vector3f.YP.rotationDegrees(0));
                            case SOUTH -> new LockSetting(new Vec3(0.5, 0.5, 1), Vector3f.YP.rotationDegrees(0));
                            case WEST -> new LockSetting(new Vec3(0, 0.5, 0.5), Vector3f.YP.rotationDegrees(90));
                            case EAST -> new LockSetting(new Vec3(1, 0.5, 0.5), Vector3f.YP.rotationDegrees(90));
                        };
                    }
                } catch (Exception e) {
                    return defaultDynamicSetting(level, state.getBlock().asItem().getDefaultInstance(), partialTick);
                }
            }

            public boolean containsPos(BlockPos pos, BlockPos pos1, BlockState state) {
                return true;
            }

            public List<BlockPos> poses(BlockPos pos, BlockState state) {
                return List.of(pos);
            }

            public void unlockBlock(Player player, Level level, BlockPos pos, BlockState state, ItemStack lockItem) {
                unlock(pos);
            }
        };
    }

    public static IBlockLocking defaultDoorBlock() {
        return new IBlockLocking() {
            public LockSetting getDefaultCustomLockSetting(Level level, BlockPos pos, BlockState state, float partialTick) {
                try {
                    Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

                    if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER)) {
                        return switch (direction) {
                            case NORTH, UP, DOWN -> new LockSetting(new Vec3(0.5, 0, 0.8), Vector3f.YP.rotationDegrees(0));
                            case SOUTH -> new LockSetting(new Vec3(0.5, 0, 0.2), Vector3f.YP.rotationDegrees(0));
                            case WEST -> new LockSetting(new Vec3(0.8, 0, 0.5), Vector3f.YP.rotationDegrees(90));
                            case EAST -> new LockSetting(new Vec3(0.2, 0, 0.5), Vector3f.YP.rotationDegrees(90));
                        };
                    } else {
                        return switch (direction) {
                            case NORTH, UP, DOWN -> new LockSetting(new Vec3(0.5, 1, 1), Vector3f.YP.rotationDegrees(0));
                            case SOUTH -> new LockSetting(new Vec3(0.5, 1, 0), Vector3f.YP.rotationDegrees(0));
                            case WEST -> new LockSetting(new Vec3(1, 1, 0.5), Vector3f.YP.rotationDegrees(90));
                            case EAST -> new LockSetting(new Vec3(0, 1, 0.5), Vector3f.YP.rotationDegrees(90));
                        };
                    }
                } catch (Exception e) {
                    return new LockSetting(new Vec3(1, 1, 0.5), Vector3f.YP.rotationDegrees(0));
                }
            }

            public boolean containsPos(BlockPos pos, BlockPos pos1, BlockState state) {
                if (state.getBlock() instanceof TrapDoorBlock)
                    return true;

                if (pos.equals(pos1))
                    return true;

                if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER))
                    return pos.below().equals(pos1);
                else
                    return pos.above().equals(pos1);
            }

            public List<BlockPos> poses(BlockPos pos, BlockState state) {
                if (state.getBlock() instanceof TrapDoorBlock)
                    return List.of(pos);

                if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER))
                    return List.of(pos, pos.below());
                else
                    return List.of(pos, pos.above());
            }

            public void unlockBlock(Player player, Level level, BlockPos pos, BlockState state, ItemStack lockItem) {
                unlock(pos);

                if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER))
                    unlock(pos.below());
                else unlock(pos.above());
            }
        };
    }

    public static LockSetting defaultDynamicSetting(Level level, ItemStack stack, float partialTick) {
        BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(stack, level, null, 0);
        Vec3 vec3 = new Vec3(0.5, 1.125F + bakedmodel.getTransforms().getTransform(ItemTransforms.TransformType.FIXED).scale.y() + Math.sin((partialTick + (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0)) * 0.15F) * 0.1F, 0.5);

        return new LockSetting(vec3, Vector3f.YP.rotationDegrees((partialTick + (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0F) + level.getRandom().nextFloat()) * 2));
    }

    public static LockSetting defaultDynamicSetting(Level level, Vec3 vec3, ItemStack stack, float partialTick) {
        BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(stack, level, null, 0);
        Vec3 vec = new Vec3(vec3.x, vec3.y + bakedmodel.getTransforms().getTransform(ItemTransforms.TransformType.FIXED).scale.y() + Math.sin((partialTick + (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0)) * 0.15F) * 0.1F, vec3.z);

        return new LockSetting(vec, Vector3f.YP.rotationDegrees((partialTick + (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0F)) * 2));
    }
}

