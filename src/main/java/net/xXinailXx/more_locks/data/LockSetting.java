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
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record LockSetting(Vec3 pos, Quaternion rot) {
    public static IBlockLocking defaultOtherBlock() {
        return new IBlockLocking() {
            public LockSetting getDefaultCustomLockSetting(Level level, BlockPos pos, BlockState state, float partialTick) {
                try {
                    if (state.getBlock() instanceof TrapDoorBlock) {
                        if (state.getValue(BlockStateProperties.HALF).equals(Half.TOP))
                            return new LockSetting(new Vec3(0.5, 1, 0.5), Vector3f.XP.rotationDegrees(90));
                        else
                            return new LockSetting(new Vec3(0.5, 0.2, 0.5), Vector3f.XP.rotationDegrees(90));
                    }

                    Direction direction = state.getValue(BlockStateProperties.FACING);
                    BlockEntity entity = level.getChunkAt(pos).getBlockEntity(pos);

                    if (entity instanceof BaseContainerBlockEntity || state.getBlock() instanceof TrapDoorBlock) {
                        return defaultDynamicSetting(level, LocksData.getLockables().get(pos).getB(), partialTick);
                    } else if (state.getBlock() instanceof FenceGateBlock) {
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
                    return defaultDynamicSetting(level, LocksData.getLockables().get(pos).getB(), partialTick);
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

    public static IBlockLocking defaultBlock() {
        return new IBlockLocking() {
            public LockSetting getDefaultCustomLockSetting(Level level, BlockPos pos, BlockState state, float partialTick) {
                try {
                    if (state.getBlock() instanceof DoorBlock) {
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
                    } else if (state.getBlock() instanceof AbstractChestBlock<?>) {
                        ChestType type = state.getValue(BlockStateProperties.CHEST_TYPE);

                        if (!type.equals(ChestType.SINGLE)) {
                            Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

                            if (type.equals(ChestType.RIGHT)) {
                                Vec3 vec3 = switch (direction) {
                                    case NORTH, UP, DOWN -> new Vec3(0, 1, 0.5);
                                    case SOUTH -> new Vec3(1, 1, 0.5);
                                    case WEST -> new Vec3(0.5, 1, 1);
                                    case EAST -> new Vec3(0.5, 1, 0);
                                };

                                return LockSetting.defaultDynamicSetting(level, vec3, state.getBlock().asItem().getDefaultInstance(), partialTick);
                            } else {
                                return null;
                            }
                        }
                    } else if (state.getBlock() instanceof BedBlock) {
                        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                        boolean head = state.getValue(BlockStateProperties.BED_PART).equals(BedPart.HEAD);
                        BlockPos neighboringPos = switch (direction) {
                            case NORTH, DOWN, UP -> head ? pos.south() : pos.north();
                            case SOUTH -> head ? pos.north() : pos.south();
                            case WEST -> head ? pos.east() : pos.west();
                            case EAST -> head ? pos.west() : pos.east();
                        };

                        if (head) {
                            Vec3 vec3 = switch (direction) {
                                case NORTH, UP, DOWN -> new Vec3(0.5, 1, 1);
                                case SOUTH -> new Vec3(0.5, 1, 0);
                                case WEST -> new Vec3(1, 1, 0.5);
                                case EAST -> new Vec3(0, 1, 0.5);
                            };

                            return LockSetting.defaultDynamicSetting(level, vec3, state.getBlock().asItem().getDefaultInstance(), partialTick);
                        } else {
                            return null;
                        }
                    } else {
                        try {
                            DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);

                            if (half.equals(DoubleBlockHalf.UPPER))
                                return defaultDynamicSetting(level, LocksData.getLockables().get(pos).getB(), partialTick);
                            else
                                return defaultDynamicSetting(level, new Vec3(pos.getX(), pos.getY(), pos.getZ()).add(0, 1, 0), LocksData.getLockables().get(pos).getB(), partialTick);
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                }

                return defaultOtherBlock().getDefaultCustomLockSetting(level, pos, state, partialTick);
            }

            public boolean containsPos(BlockPos pos, BlockPos pos1, BlockState state) {
                if (pos.equals(pos1))
                    return true;

                try {
                    if (state.getBlock() instanceof AbstractChestBlock<?>) {
                        ChestType type = state.getValue(BlockStateProperties.CHEST_TYPE);

                        if (!type.equals(ChestType.SINGLE)) {
                            Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                            boolean right = state.getValue(BlockStateProperties.CHEST_TYPE).equals(ChestType.RIGHT);

                            BlockPos neighboringPos = switch (direction) {
                                case NORTH, DOWN, UP -> right ? pos.west() : pos.east();
                                case SOUTH -> right ? pos.east() : pos.west();
                                case WEST -> right ? pos.south() : pos.north();
                                case EAST -> right ? pos.north() : pos.south();
                            };

                            return pos.equals(pos1) || neighboringPos.equals(pos1);
                        } else {
                            return pos.equals(pos1);
                        }
                    } else if (state.getBlock() instanceof BedBlock) {
                        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                        boolean head = state.getValue(BlockStateProperties.BED_PART).equals(BedPart.HEAD);
                        BlockPos neighboringPos = switch (direction) {
                            case NORTH, DOWN, UP -> head ? pos.south() : pos.north();
                            case SOUTH -> head ? pos.north() : pos.south();
                            case WEST -> head ? pos.east() : pos.west();
                            case EAST -> head ? pos.west() : pos.east();
                        };

                        return pos.equals(pos1) || neighboringPos.equals(pos1);
                    } else {
                        try {
                            DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);

                            if (half.equals(DoubleBlockHalf.UPPER))
                                return pos.equals(pos1);
                            else
                                return pos.below().equals(pos1);
                        } catch (Exception e) {
                            return defaultOtherBlock().containsPos(pos, pos1, state);
                        }
                    }
                } catch (Exception e) {
                    return defaultOtherBlock().containsPos(pos, pos1, state);
                }
            }

            public List<BlockPos> poses(BlockPos pos, BlockState state) {
                try {
                    if (state.getBlock() instanceof AbstractChestBlock<?>) {
                        ChestType type = state.getValue(BlockStateProperties.CHEST_TYPE);

                        if (!type.equals(ChestType.SINGLE)) {
                            Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                            boolean right = state.getValue(BlockStateProperties.CHEST_TYPE).equals(ChestType.RIGHT);

                            BlockPos neighboringPos = switch (direction) {
                                case NORTH, DOWN, UP -> right ? pos.west() : pos.east();
                                case SOUTH -> right ? pos.east() : pos.west();
                                case WEST -> right ? pos.south() : pos.north();
                                case EAST -> right ? pos.north() : pos.south();
                            };

                            return List.of(pos, neighboringPos);
                        } else {
                            return List.of(pos);
                        }
                    } else if (state.getBlock() instanceof BedBlock) {
                        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                        boolean head = state.getValue(BlockStateProperties.BED_PART).equals(BedPart.HEAD);
                        BlockPos neighboringPos = switch (direction) {
                            case NORTH, DOWN, UP -> head ? pos.south() : pos.north();
                            case SOUTH -> head ? pos.north() : pos.south();
                            case WEST -> head ? pos.east() : pos.west();
                            case EAST -> head ? pos.west() : pos.east();
                        };

                        return List.of(pos, neighboringPos);
                    } else {
                        try {
                            if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER))
                                return List.of(pos, pos.below());
                            else
                                return List.of(pos, pos.above());
                        } catch (Exception e) {
                            return defaultOtherBlock().poses(pos, state);
                        }
                    }
                } catch (Exception e) {
                    return defaultOtherBlock().poses(pos, state);
                }
            }

            public void unlockBlock(Player player, Level level, BlockPos pos, BlockState state, ItemStack lockItem) {
                unlock(pos);

                try {
                    if (state.getBlock() instanceof AbstractChestBlock<?>) {
                        ChestType type = state.getValue(BlockStateProperties.CHEST_TYPE);

                        if (!type.equals(ChestType.SINGLE)) {
                            Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                            boolean right = state.getValue(BlockStateProperties.CHEST_TYPE).equals(ChestType.RIGHT);

                            BlockPos neighboringPos = switch (direction) {
                                case NORTH, DOWN, UP -> right ? pos.west() : pos.east();
                                case SOUTH -> right ? pos.east() : pos.west();
                                case WEST -> right ? pos.south() : pos.north();
                                case EAST -> right ? pos.north() : pos.south();
                            };

                            unlock(neighboringPos);
                        }
                    } else if (state.getBlock() instanceof BedBlock) {
                        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                        boolean head = state.getValue(BlockStateProperties.BED_PART).equals(BedPart.HEAD);
                        BlockPos neighboringPos = switch (direction) {
                            case NORTH, DOWN, UP -> head ? pos.south() : pos.north();
                            case SOUTH -> head ? pos.north() : pos.south();
                            case WEST -> head ? pos.east() : pos.west();
                            case EAST -> head ? pos.west() : pos.east();
                        };

                        unlock(neighboringPos);
                    } else {
                        try {
                            if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER))
                                unlock(pos.below());
                            else
                                unlock(pos.above());
                        } catch (Exception e) {
                            defaultOtherBlock().unlockBlock(player, level, pos, state, lockItem);
                        }
                    }
                } catch (Exception e) {
                    defaultOtherBlock().unlockBlock(player, level, pos, state, lockItem);
                }
            }
        };
    }

    public static LockSetting defaultDynamicSetting(Level level, ItemStack stack, float partialTick) {
        BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(stack, level, null, 0);
        Vec3 vec3 = new Vec3(0.5, 0.5 + bakedmodel.getTransforms().getTransform(ItemTransforms.TransformType.FIXED).scale.y() + Math.sin((partialTick + (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0)) * 0.15) * 0.1, 0.5);

        return new LockSetting(vec3, Vector3f.YP.rotationDegrees((partialTick + (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0) + level.getRandom().nextFloat()) * 2));
    }

    public static LockSetting defaultDynamicSetting(Level level, Vec3 vec3, ItemStack stack, float partialTick) {
        BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(stack, level, null, 0);
        Vec3 vec = new Vec3(vec3.x, vec3.y + bakedmodel.getTransforms().getTransform(ItemTransforms.TransformType.FIXED).scale.y() + Math.sin((partialTick + (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0)) * 0.15) * 0.1, vec3.z);

        return new LockSetting(vec, Vector3f.YP.rotationDegrees((partialTick + (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0)) * 2 + level.getRandom().nextFloat()));
    }
}

