package net.xXinailXx.more_locks;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.xXinailXx.more_locks.config.MLCommonConfig;
import net.xXinailXx.more_locks.data.LocksData;
import net.xXinailXx.more_locks.init.ItemRegistry;

@Mod(MoreLocks.MODID)
public class MoreLocks {
    public static final String MODID = "more_locks";
    public static final CreativeModeTab ITEM_TAB = new CreativeModeTab("more_locks_tab") {
        public ItemStack makeIcon() {
            return ItemRegistry.LOCK_PICK.get().getDefaultInstance();
        }
    };

    public MoreLocks() {
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MLCommonConfig.SPEC, "more_locks-common.toml");

        ItemRegistry.register();
    }

    @Mod.EventBusSubscriber
    public static class Events {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void joinWorld(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();

            if (player == null)
                return;

            LocksData.readPatterns(player.getLevel());
        }
    }
}
