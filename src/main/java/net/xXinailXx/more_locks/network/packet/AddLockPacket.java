package net.xXinailXx.more_locks.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.xXinailXx.enderdragonlib.capability.ServerCapManager;
import net.xXinailXx.more_locks.data.IBlockLocking;
import net.xXinailXx.more_locks.data.LockSetting;
import net.xXinailXx.more_locks.data.LocksData;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;
import oshi.util.tuples.Pair;

import java.util.List;

public class AddLockPacket implements IPacket {
    private BlockPos pos;
    private ItemStack stack;

    public AddLockPacket(BlockPos pos, ItemStack stack) {
        this.pos = pos;
        this.stack = stack;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeItem(this.stack);
    }

    public void read(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.stack = buf.readItem();
    }

    public void serverExecute(PacketContext ctx) {
        ServerPlayer player = ctx.getSender();
        ServerLevel level = player.getLevel();
        BlockState state = level.getBlockState(this.pos);
        Block block = state.getBlock();
        CompoundTag tag = ServerCapManager.getOrCreateData("more_locks_locks_data");

        if (block instanceof IBlockLocking locking1) {
            for (BlockPos pos1 : locking1.poses(this.pos, state)) {
                tag.putString(String.valueOf(pos1.asLong()), ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());

                LocksData.LOCKABLES.put(pos1, new Pair<>(locking1, stack));
            }
        } else if (block instanceof AbstractChestBlock<?>) {
            try {
                ChestType type = state.getValue(BlockStateProperties.CHEST_TYPE);
                Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                boolean right = type.equals(ChestType.RIGHT);

                if (!type.equals(ChestType.SINGLE)) {
                    BlockPos neighboringPos = switch (direction) {
                        case NORTH, DOWN, UP -> type.equals(ChestType.RIGHT) ? this.pos.west() : this.pos.east();
                        case SOUTH -> type.equals(ChestType.RIGHT) ? this.pos.east() : this.pos.west();
                        case WEST -> type.equals(ChestType.RIGHT) ? this.pos.south() : this.pos.north();
                        case EAST -> type.equals(ChestType.RIGHT) ? this.pos.north() : this.pos.south();
                    };

                    IBlockLocking mainChest = new IBlockLocking() {
                        public LockSetting getDefaultCustomLockSetting(Level level, BlockPos pos, BlockState state, float partialTick) {
                            Vec3 vec3 = null;

                            if (state.getValue(BlockStateProperties.CHEST_TYPE).equals(ChestType.RIGHT)) {
                                vec3 = switch (direction) {
                                    case NORTH, UP, DOWN -> new Vec3(0, 1.125F, 0.5);
                                    case SOUTH -> new Vec3(1, 1.125F, 0.5);
                                    case WEST -> new Vec3(0.5, 1.125F, 1);
                                    case EAST -> new Vec3(0.5, 1.125F, 0);
                                };
                            } else {
                                vec3 = switch (direction) {
                                    case NORTH, UP, DOWN -> new Vec3(1, 1.125F, 0.5);
                                    case SOUTH -> new Vec3(0, 1.125F, 0.5);
                                    case WEST -> new Vec3(0.5, 1.125F, 0);
                                    case EAST -> new Vec3(0.5, 1.125F, 1);
                                };
                            }

                            return LockSetting.defaultDynamicSetting(level, vec3, state.getBlock().asItem().getDefaultInstance(), partialTick);
                        }

                        public boolean containsPos(BlockPos pos, BlockPos pos1, BlockState state) {
                            return pos.equals(pos1) || neighboringPos.equals(pos1);
                        }

                        public List<BlockPos> poses(BlockPos pos, BlockState state) {
                            return List.of(pos, neighboringPos);
                        }

                        public void unlockBlock(Player player, Level level, BlockPos pos, BlockState state, ItemStack lockItem) {
                            unlock(pos);
                            unlock(neighboringPos);
                        }
                    };

                    tag.putString(String.valueOf(this.pos.asLong()), ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                    tag.putString(String.valueOf(neighboringPos.asLong()), ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());

                    LocksData.LOCKABLES.put(this.pos, new Pair<>(mainChest, stack));
                    LocksData.LOCKABLES.put(neighboringPos, new Pair<>(mainChest, stack));
                } else {
                    tag.putString(String.valueOf(this.pos.asLong()), ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());

                    LocksData.LOCKABLES.put(this.pos, new Pair<>(LockSetting.defaultBlock(), stack));
                }
            } catch (Exception e) {
                tag.putString(String.valueOf(this.pos.asLong()), ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());

                LocksData.LOCKABLES.put(this.pos, new Pair<>(LockSetting.defaultBlock(), stack));
            }
        } else if (block instanceof TrapDoorBlock) {
            IBlockLocking locking = LockSetting.defaultBlock();

            tag.putString(String.valueOf(this.pos.asLong()), ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());

            LocksData.LOCKABLES.put(this.pos, new Pair<>(locking, stack));
        } else {
            IBlockLocking locking;

            try {
                state.getValue(BlockStateProperties.HORIZONTAL_FACING);

                locking = LockSetting.defaultDoorBlock();
            } catch (Exception e) {
                locking = LockSetting.defaultBlock();
            }

            tag.putString(String.valueOf(pos.asLong()), ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
            LocksData.LOCKABLES.put(this.pos, new Pair<>(locking, stack));

            for (BlockPos pos1 : locking.poses(this.pos, state)) {
                tag.putString(String.valueOf(pos1.asLong()), ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());

                LocksData.LOCKABLES.put(pos1, new Pair<>(locking, stack));
            }
        }

        ServerCapManager.addServerData("more_locks_locks_data", tag);

        if (!player.isCreative())
            stack.split(1);
    }
}
