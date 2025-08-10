package net.xXinailXx.more_locks.init;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xXinailXx.more_locks.MoreLocks;
import net.xXinailXx.more_locks.config.MLCommonConfig;
import net.xXinailXx.more_locks.item.AutoLockPickItem;
import net.xXinailXx.more_locks.item.LockItem;
import net.xXinailXx.more_locks.item.LockPickItem;
import oshi.util.tuples.Pair;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MoreLocks.MODID);

    public static final RegistryObject<Item> LOCK_PICK = ITEMS.register("lock_pick", () -> new LockPickItem(new Item.Properties().tab(MoreLocks.ITEM_TAB).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> AUTO_LOCK_PICK = ITEMS.register("auto_lock_pick", () -> new AutoLockPickItem(new Item.Properties().tab(MoreLocks.ITEM_TAB).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> WIRE = ITEMS.register("wire", () -> new Item(new Item.Properties().tab(MoreLocks.ITEM_TAB)));
    public static final RegistryObject<Item> COPPER_LOCK = ITEMS.register("copper_lock", () -> new LockItem(new Item.Properties().tab(MoreLocks.ITEM_TAB).setNoRepair()) {
        public Pair<Integer, Integer> getCountLatches() {
            return new Pair<>(MLCommonConfig.COPPER_LOCK_MIN_LATCHES.get(), MLCommonConfig.COPPER_LOCK_MAX_LATCHES.get());
        }
    });
    public static final RegistryObject<Item> IRON_LOCK = ITEMS.register("iron_lock", () -> new LockItem(new Item.Properties().tab(MoreLocks.ITEM_TAB).setNoRepair()) {
        public Pair<Integer, Integer> getCountLatches() {
            return new Pair<>(MLCommonConfig.IRON_LOCK_MIN_LATCHES.get(), MLCommonConfig.IRON_LOCK_MAX_LATCHES.get());
        }
    });
    public static final RegistryObject<Item> GOLD_LOCK = ITEMS.register("gold_lock", () -> new LockItem(new Item.Properties().tab(MoreLocks.ITEM_TAB).setNoRepair()) {
        public Pair<Integer, Integer> getCountLatches() {
            return new Pair<>(MLCommonConfig.GOLD_LOCK_MIN_LATCHES.get(), MLCommonConfig.GOLD_LOCK_MAX_LATCHES.get());
        }
    });
    public static final RegistryObject<Item> DIAMOND_LOCK = ITEMS.register("diamond_lock", () -> new LockItem(new Item.Properties().tab(MoreLocks.ITEM_TAB).setNoRepair()) {
        public Pair<Integer, Integer> getCountLatches() {
            return new Pair<>(MLCommonConfig.DIAMOND_LOCK_MIN_LATCHES.get(), MLCommonConfig.DIAMOND_LOCK_MAX_LATCHES.get());
        }
    });
    public static final RegistryObject<Item> NETHERITE_LOCK = ITEMS.register("netherite_lock", () -> new LockItem(new Item.Properties().tab(MoreLocks.ITEM_TAB).setNoRepair()) {
        public Pair<Integer, Integer> getCountLatches() {
            return new Pair<>(MLCommonConfig.NETHERITE_LOCK_MIN_LATCHES.get(), MLCommonConfig.NETHERITE_LOCK_MAX_LATCHES.get());
        }
    });

    public static void register() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
