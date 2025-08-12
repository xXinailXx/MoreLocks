package net.xXinailXx.more_locks.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.xXinailXx.more_locks.api.event.client.LockEvent;
import net.xXinailXx.more_locks.data.IBlockLocking;
import oshi.util.tuples.Pair;

public class AutoLockPickItem extends LockPickItem {
    public AutoLockPickItem(Properties properties) {
        super(properties);
    }

    public boolean useOnLock(Player player, Level level, BlockPos pos, BlockState state, Pair<IBlockLocking, ItemStack> pair) {
        if (level.isClientSide)
            return false;

        pair.getA().unlockBlock(player, level, pos, state, pair.getB());

        if (player.getInventory().getFreeSlot() > -1)
            player.addItem(pair.getB());
        else
            player.drop(pair.getB(), false, false);

        MinecraftForge.EVENT_BUS.post(new LockEvent.Unlock(player, pos, pair.getA(), pair.getB()));

        return true;
    }
}
