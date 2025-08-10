package net.xXinailXx.more_locks.data;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraftforge.registries.ForgeRegistries;
import net.xXinailXx.enderdragonlib.capability.ServerCapManager;
import net.xXinailXx.more_locks.config.MLCommonConfig;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.regex.Pattern;

public class LocksData {
    public static final Map<BlockPos, Pair<IBlockLocking, ItemStack>> LOCKABLES = new HashMap<>();
    private static final List<String> blocks = new ArrayList<>();
    private static final List<Pattern> patterns = new ArrayList<>();

    public static void readPatterns(Level level) {
        for (String id : MLCommonConfig.LOCKABLE_BLOCKS.get()) {
            if (id.contains(":")) {
                blocks.add(id);

                continue;
            }

            patterns.add(Pattern.compile(id));
        }

        CompoundTag tag = ServerCapManager.getOrCreateData("more_locks_locks_data");

        for (String key : tag.getAllKeys()) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString(key)));

            if (item == null)
                continue;

            BlockPos pos = BlockPos.of(Long.valueOf(key));
            BlockState state = level.getBlockState(pos);
            IBlockLocking locking;

            if (state.getBlock() instanceof IBlockLocking locking1) {
                locking = locking1;
            } else {
                try {
                    Half half = state.getValue(BlockStateProperties.HALF);
                    locking = LockSetting.defaultDoorBlock();
                } catch (Exception e) {
                    locking = LockSetting.defaultBlock();
                }
            }

            locking.setData(tag.getCompound("data"));

            LOCKABLES.put(pos, new Pair<>(locking, item.getDefaultInstance()));
        }
    }

    public static boolean canLock(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        String name = getBlockName(block);

        if (blocks.contains(name))
            return true;

        for (Pattern p : patterns)
            if (p.matcher(name).matches())
                return true;

        return false;
    }

    @Nullable
    public static Pair<Boolean, Pair<IBlockLocking, ItemStack>> containsLock(Level level, BlockPos pos) {
        for (BlockPos pos1 : LOCKABLES.keySet())
            if (pos1.equals(pos) || LOCKABLES.get(pos1).getA().containsPos(pos, pos1, level.getBlockState(pos)))
                return new Pair<>(true, LOCKABLES.get(pos));

        return null;
    }

    public static String getBlockName(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block).toString();
    }
}
