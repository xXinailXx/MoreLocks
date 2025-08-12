package net.xXinailXx.more_locks.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.xXinailXx.more_locks.api.event.client.LockEvent;
import net.xXinailXx.more_locks.data.IBlockLocking;
import net.xXinailXx.more_locks.data.LocksData;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;
import oshi.util.tuples.Pair;

public class UnblockingBlockPacket implements IPacket {
    private BlockPos pos;

    public UnblockingBlockPacket(BlockPos pos) {
        this.pos = pos;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public void read(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    public void serverExecute(PacketContext ctx) {
        ServerPlayer player = ctx.getSender();
        ServerLevel level = player.getLevel();

        Pair<IBlockLocking, ItemStack> pair = LocksData.containsLock(level, this.pos);

        if (pair == null)
            return;

        pair.getA().unlockBlock(player, level, this.pos, level.getBlockState(this.pos), pair.getB());

        if (player.getInventory().getFreeSlot() > -1)
            player.addItem(pair.getB());
        else
            player.drop(pair.getB(), false, false);

        MinecraftForge.EVENT_BUS.post(new LockEvent.Unlock(player, this.pos, pair.getA(), pair.getB()));
    }
}
