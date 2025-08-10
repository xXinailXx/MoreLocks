package net.xXinailXx.more_locks.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.xXinailXx.enderdragonlib.capability.ServerCapManager;
import net.xXinailXx.more_locks.MoreLocks;
import net.xXinailXx.more_locks.data.LocksData;
import net.xXinailXx.more_locks.data.IBlockLocking;
import net.xXinailXx.more_locks.network.packet.AddLockPacket;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.net.Network;
import oshi.util.tuples.Pair;

@Mod.EventBusSubscriber
public abstract class LockItem extends Item {
    public LockItem(Properties property) {
        super(property);
    }

    public abstract Pair<Integer, Integer> getCountLatches();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void clickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();

        if (player == null)
            return;

        Level level = player.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (!LocksData.canLock(level, pos))
            return;

        ItemStack stack = event.getItemStack();

        if (stack.getItem() instanceof LockItem && player.isShiftKeyDown()) {
            Network.sendToServer(new AddLockPacket(pos, stack));

            event.setCanceled(true);

            return;
        }

        Pair<Boolean, Pair<IBlockLocking, ItemStack>> booleanPair = LocksData.containsLock(level, pos);

        if (booleanPair == null || booleanPair.getB() == null)
            return;

        if (stack.getItem() instanceof LockPickItem item) {
            player.swing(InteractionHand.MAIN_HAND);

            event.setCanceled(item.useOnLock(player, level, pos, state, booleanPair.getB()));
        } else {
            player.displayClientMessage(Component.translatable("message." + MoreLocks.MODID + ".block_lock", Component.translatable("message." + MoreLocks.MODID + ".block_lock.type_" + ForgeRegistries.ITEMS.getKey(booleanPair.getB().getB().getItem()).getPath()).getString()), true);
            player.swing(InteractionHand.MAIN_HAND);

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreaking(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();

        if (player == null || player.getLevel().isClientSide)
            return;

        if (LocksData.containsLock(player.getLevel(), event.getPos()).getA()) {
            if (!player.isCreative()) {
                event.setNewSpeed(0F);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent()
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();

        if (player == null || player.getLevel().isClientSide)
            return;

        Pair<Boolean, @Nullable Pair<IBlockLocking, ItemStack>> booleanPair = LocksData.containsLock(player.getLevel(), event.getPos());

        if (booleanPair.getA()) {
            if (!player.isCreative()) {
                event.setCanceled(true);
            } else {
                Pair<IBlockLocking, ItemStack> pair = booleanPair.getB();

                if (!(pair.getB().getItem() instanceof LockItem))
                    return;

                BlockPos pos = event.getPos();

                if (player.getInventory().getFreeSlot() > -1)
                    player.addItem(pair.getB());
                else
                    player.drop(pair.getB(), false, false);

                LocksData.LOCKABLES.remove(event.getPos());

                CompoundTag tag = ServerCapManager.getOrCreateData("more_locks_locks_data");

                tag.remove(String.valueOf(pos.asLong()));

                ServerCapManager.addServerData("more_locks_locks_data", tag);
            }
        }
    }
}
