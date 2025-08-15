package net.xXinailXx.more_locks.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.xXinailXx.enderdragonlib.capability.ServerCapManager;
import net.xXinailXx.more_locks.client.gui.LockScreen;
import net.xXinailXx.more_locks.data.IBlockLocking;
import oshi.util.tuples.Pair;

public class LockPickItem extends Item {
    public LockPickItem(Properties properties) {
        super(properties);
    }

    public boolean useOnLock(Player player, Level level, BlockPos pos, BlockState state, Pair<IBlockLocking, ItemStack> pair) {
        CompoundTag tag = ServerCapManager.getOrCreateData("more_locks_locks_data");

        if (!tag.contains(String.valueOf(pos.asLong())))
            return false;

        if (level.isClientSide)
            Minecraft.getInstance().setScreen(new LockScreen(pos, tag.getCompound(String.valueOf(pos.asLong())).getInt("count_latches")));

        return true;
    }
}
