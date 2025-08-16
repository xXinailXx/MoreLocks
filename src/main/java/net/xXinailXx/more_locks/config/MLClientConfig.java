package net.xXinailXx.more_locks.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class MLClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> MENU_TYPE;
    public static final ForgeConfigSpec.ConfigValue<Integer> CHANGE_BREAKING_LOCKPICK_SUCCESS;
    public static final ForgeConfigSpec.ConfigValue<Integer> CHANGE_BREAKING_LOCKPICK_FAIL;
    public static final ForgeConfigSpec.ConfigValue<Integer> CHANGE_BASE_ACCELERATION;

    static {
        BUILDER.push("Common config for More Locks");

        MENU_TYPE = BUILDER.comment("Write the name of the folder in which the textures for the menu will be located.")
                .comment("Enter your default_0 or default_1 to choose from several default menu texts. To change the menu add textures ending with:")
                .comment("_background - background")
                .comment("_latch_blocking - locked latch")
                .comment("_latch_unblocking - unlocked latch")
                .comment("_lock_pick - lock pick")
                .define("menu_type", "default_0");

        BUILDER.comment("");

        CHANGE_BREAKING_LOCKPICK_SUCCESS = BUILDER.comment("The chance that a master lock pick break when the lock latch is opened correctly.")
                .defineInRange("chance_breaking_success", 5, 0, 100);
        CHANGE_BREAKING_LOCKPICK_FAIL = BUILDER.comment("The chance with which the lock pick can break when the lock latch is opened correctly or incorrectly.")
                .defineInRange("chance_breaking_fail", 90, 0, 100);

        CHANGE_BASE_ACCELERATION = BUILDER.comment("Base lock pick rotation speed")
                .define("base_acceleration", 6);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
