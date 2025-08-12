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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.xXinailXx.more_locks.MoreLocks;
import net.xXinailXx.more_locks.api.event.client.LockEvent;
import net.xXinailXx.more_locks.config.MLClientConfig;
import net.xXinailXx.more_locks.config.MLCommonConfig;
import net.xXinailXx.more_locks.data.LocksData;
import net.xXinailXx.more_locks.data.IBlockLocking;
import net.xXinailXx.more_locks.network.packet.AddLockPacket;
import org.zeith.hammerlib.net.Network;
import oshi.util.tuples.Pair;

import java.util.Random;

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

        Pair<IBlockLocking, ItemStack> pair = LocksData.containsLock(level, pos);

        if (pair == null || pair.getB() == null)
            return;

        if (stack.getItem() instanceof LockPickItem item) {
            player.swing(InteractionHand.MAIN_HAND);

            event.setCanceled(item.useOnLock(player, level, pos, state, pair));
        } else {
            player.displayClientMessage(Component.translatable("message." + MoreLocks.MODID + ".block_lock", Component.translatable("message." + MoreLocks.MODID + ".block_lock.type_" + ForgeRegistries.ITEMS.getKey(pair.getB().getItem()).getPath()).getString()), true);
            player.swing(InteractionHand.MAIN_HAND);

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreaking(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();

        if (player == null)
            return;

        if (LocksData.containsLock(player.getLevel(), event.getPos()) != null) {
            if (!player.isCreative()) {
                event.setNewSpeed(0F);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();

        if (player == null || !player.isCreative())
            return;

        BlockPos pos = event.getPos();
        Pair<IBlockLocking, ItemStack> pair = LocksData.containsLock(player.getLevel(), pos);

        if (pair == null)
            return;

        pair.getA().unlockBlock(player, player.getLevel(), pos, player.getLevel().getBlockState(pos), pair.getB());

        MinecraftForge.EVENT_BUS.post(new LockEvent.Unlock(player, pos, pair.getA(), pair.getB()));
    }
}
