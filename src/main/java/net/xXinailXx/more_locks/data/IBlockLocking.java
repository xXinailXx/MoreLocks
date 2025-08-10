package net.xXinailXx.more_locks.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.xXinailXx.enderdragonlib.capability.ServerCapManager;
import oshi.util.tuples.Pair;

import java.util.List;

public interface IBlockLocking {
    CompoundTag data = new CompoundTag();

    LockSetting getDefaultCustomLockSetting(Level level, BlockPos pos, BlockState state, float partialTick);

    boolean containsPos(BlockPos pos, BlockPos pos1, BlockState state);

    List<BlockPos> poses(BlockPos pos, BlockState state);

    void unlockBlock(Player player, Level level, BlockPos pos, BlockState state, ItemStack lockItem);

    default void setData(CompoundTag tag) {
        for (String key : data.getAllKeys())
            data.remove(key);

        for (String key : tag.getAllKeys())
            data.put(key, tag.get(key));
    }

    default void unlock(BlockPos pos) {
        LocksData.LOCKABLES.remove(pos);

        CompoundTag tag = ServerCapManager.getOrCreateData("more_locks_locks_data");

        tag.remove(String.valueOf(pos.asLong()));

        ServerCapManager.addServerData("more_locks_locks_data", tag);
    }
}

