package net.xXinailXx.more_locks.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class MLCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOCKABLE_BLOCKS;
    public static final ForgeConfigSpec.ConfigValue<Integer> COPPER_LOCK_MIN_LATCHES;
    public static final ForgeConfigSpec.ConfigValue<Integer> COPPER_LOCK_MAX_LATCHES;
    public static final ForgeConfigSpec.ConfigValue<Integer> IRON_LOCK_MIN_LATCHES;
    public static final ForgeConfigSpec.ConfigValue<Integer> IRON_LOCK_MAX_LATCHES;
    public static final ForgeConfigSpec.ConfigValue<Integer> GOLD_LOCK_MIN_LATCHES;
    public static final ForgeConfigSpec.ConfigValue<Integer> GOLD_LOCK_MAX_LATCHES;
    public static final ForgeConfigSpec.ConfigValue<Integer> DIAMOND_LOCK_MIN_LATCHES;
    public static final ForgeConfigSpec.ConfigValue<Integer> DIAMOND_LOCK_MAX_LATCHES;
    public static final ForgeConfigSpec.ConfigValue<Integer> NETHERITE_LOCK_MIN_LATCHES;
    public static final ForgeConfigSpec.ConfigValue<Integer> NETHERITE_LOCK_MAX_LATCHES;

    static {
        BUILDER.push("Common config for More Locks");

        LOCKABLE_BLOCKS = BUILDER.comment("Sets which blocks can be locked with a lock.").comment("To add a block to the list of locked ones, you can use a direct link to the block (for example: minecraft:grass_block) or a tag (for example: .*chest).")
                .comment("To customize the display of the lock, use the additional file - locks_setting.json.")
                .defineList("lockable_blocks", Lists.newArrayList(".*chest", ".*barrel", ".*door", ".*trapdoor", ".*shulker_box", ".*hopper", ".*fence_gate"), tag -> tag instanceof String);

        BUILDER.comment("");
        BUILDER.comment("The following ones set how many latches there will be in the lock.");
        BUILDER.comment("The value will be chosen randomly from the smallest to the largest.");
        BUILDER.comment("");

        COPPER_LOCK_MIN_LATCHES = BUILDER.defineInRange("copper_lock_min", 3, 2, 5);
        COPPER_LOCK_MAX_LATCHES = BUILDER.defineInRange("copper_lock_max", 5, 3, 8);

        IRON_LOCK_MIN_LATCHES = BUILDER.defineInRange("iron_lock_min", 6, 4, 8);
        IRON_LOCK_MAX_LATCHES = BUILDER.defineInRange("iron_lock_max", 8, 6, 12);

        GOLD_LOCK_MIN_LATCHES = BUILDER.defineInRange("gold_lock_min", 9, 8, 12);
        GOLD_LOCK_MAX_LATCHES = BUILDER.defineInRange("gold_lock_max", 12, 10, 15);

        DIAMOND_LOCK_MIN_LATCHES = BUILDER.defineInRange("diamond_lock_min", 12, 8, 15);
        DIAMOND_LOCK_MAX_LATCHES = BUILDER.defineInRange("diamond_lock_max", 15, 10, 20);

        NETHERITE_LOCK_MIN_LATCHES = BUILDER.defineInRange("netherite_lock_min", 18, 15, 25);
        NETHERITE_LOCK_MAX_LATCHES = BUILDER.defineInRange("netherite_lock_max", 25, 20, 30);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
