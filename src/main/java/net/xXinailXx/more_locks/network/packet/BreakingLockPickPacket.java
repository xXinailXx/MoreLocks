package net.xXinailXx.more_locks.network.packet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.xXinailXx.more_locks.item.AutoLockPickItem;
import net.xXinailXx.more_locks.item.LockPickItem;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;

public class BreakingLockPickPacket implements IPacket {
    public void serverExecute(PacketContext ctx) {
        ServerPlayer player = ctx.getSender();

        if (player.isCreative())
            return;

        ItemStack stack = null;

        if (player.getMainHandItem().getItem() instanceof LockPickItem && !(player.getMainHandItem().getItem() instanceof AutoLockPickItem)) {
            stack = player.getMainHandItem();

            stack.split(1);
            player.setItemSlot(EquipmentSlot.MAINHAND, stack);
        } else if (player.getOffhandItem().getItem() instanceof LockPickItem && !(player.getMainHandItem().getItem() instanceof AutoLockPickItem)) {
            stack = player.getOffhandItem();

            stack.split(1);
            player.setItemSlot(EquipmentSlot.MAINHAND, stack);
        } else {
            ItemStack stack1 = null;
            int slot = -1;
            boolean items = false;

            for (int i = 0; i < player.getInventory().items.size(); i++) {
                ItemStack stack2 = player.getInventory().items.get(i);

                if (stack2.getItem() instanceof LockPickItem && !(stack2.getItem() instanceof AutoLockPickItem)) {
                    slot = i;
                    stack1 = stack2;
                    items = true;

                    break;
                }
            }

            if (slot == -1) {
                for (int i = 0; i < player.getInventory().armor.size(); i++) {
                    ItemStack stack2 = player.getInventory().items.get(i);

                    if (stack2.getItem() instanceof LockPickItem && !(stack2.getItem() instanceof AutoLockPickItem)) {
                        slot = i;
                        stack1 = stack2;
                        items = false;

                        break;
                    }
                }
            }

            if (slot > -1) {
                stack.split(1);

                if (items)
                    player.getInventory().items.set(slot, stack);
                else
                    player.getInventory().armor.set(slot, stack);
            }
        }
    }
}
