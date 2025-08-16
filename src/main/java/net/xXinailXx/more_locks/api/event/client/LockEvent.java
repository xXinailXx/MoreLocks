package net.xXinailXx.more_locks.api.event.client;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.xXinailXx.more_locks.data.IBlockLocking;

@Getter
public class LockEvent extends PlayerEvent {
    private final BlockPos pos;
    private final IBlockLocking locking;
    private final ItemStack stack;

    public LockEvent(Player player, BlockPos pos, IBlockLocking locking, ItemStack stack) {
        super(player);
        this.pos = pos;
        this.locking = locking;
        this.stack = stack;
    }

    public static class Lock extends LockEvent {
        public Lock(Player player, BlockPos pos, IBlockLocking locking, ItemStack stack) {
            super(player, pos, locking, stack);
        }
    }

    public static class Unlock extends LockEvent {
        public Unlock(Player player, BlockPos pos, IBlockLocking locking, ItemStack stack) {
            super(player, pos, locking, stack);
        }
    }
}
