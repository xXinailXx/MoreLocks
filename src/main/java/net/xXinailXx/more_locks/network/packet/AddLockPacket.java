package net.xXinailXx.more_locks.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import net.xXinailXx.enderdragonlib.capability.ServerCapManager;
import net.xXinailXx.more_locks.api.event.client.LockEvent;
import net.xXinailXx.more_locks.data.IBlockLocking;
import net.xXinailXx.more_locks.data.LockSetting;
import net.xXinailXx.more_locks.data.LocksData;
import net.xXinailXx.more_locks.item.LockItem;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.Random;

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
        ItemStack stack1 = this.stack.copy();

        stack1.setCount(1);

        if (state.isAir())
            return;

        int min = ((LockItem) this.stack.getItem()).getCountLatches().getA();
        int max = ((LockItem) this.stack.getItem()).getCountLatches().getB();
        int count;

        if (min >= max)
            count = max;
        else
            count = new Random().nextInt(min, max);

        CompoundTag tag1 = new CompoundTag();

        tag1.putString("item", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
        tag1.putInt("count_latches", count);

        IBlockLocking locking = null;

        if (block instanceof IBlockLocking locking1) {
            locking = locking1;

            for (BlockPos pos1 : locking1.poses(this.pos, state)) {
                tag.put(String.valueOf(pos1.asLong()), tag1);

                LocksData.addLockables(pos1, new Pair<>(locking1, stack1));
            }
        } else {
            locking = LockSetting.defaultBlock();

            tag.put(String.valueOf(this.pos.asLong()), tag1);
            LocksData.addLockables(this.pos, new Pair<>(locking, stack1));

            for (BlockPos pos1 : locking.poses(this.pos, state)) {
                tag.put(String.valueOf(pos1.asLong()), tag1);

                LocksData.addLockables(pos1, new Pair<>(locking, stack1));
            }
        }

        ServerCapManager.addServerData("more_locks_locks_data", tag);

        if (!player.isCreative()) {
            this.stack.split(1);

            player.setItemSlot(EquipmentSlot.MAINHAND, this.stack);
        }

        MinecraftForge.EVENT_BUS.post(new LockEvent.Lock(player, this.pos, locking, stack1));
    }
}
