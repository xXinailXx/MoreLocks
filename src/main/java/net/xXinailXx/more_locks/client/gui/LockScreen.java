package net.xXinailXx.more_locks.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.xXinailXx.enderdragonlib.utils.MathUtils;
import net.xXinailXx.enderdragonlib.utils.ResourceLocationUtils;
import net.xXinailXx.more_locks.MoreLocks;
import net.xXinailXx.more_locks.client.gui.button.LatchButton;
import net.xXinailXx.more_locks.config.MLClientConfig;
import net.xXinailXx.more_locks.init.ItemRegistry;
import net.xXinailXx.more_locks.item.AutoLockPickItem;
import net.xXinailXx.more_locks.item.LockPickItem;
import net.xXinailXx.more_locks.network.packet.BreakingLockPickPacket;
import net.xXinailXx.more_locks.network.packet.UnblockingBlockPacket;
import org.zeith.hammerlib.net.Network;
import oshi.util.tuples.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LockScreen extends Screen {
    private final Minecraft MC = Minecraft.getInstance();
    @Getter
    private final List<Pair<Boolean, Integer>> latchesRots = new ArrayList<>();
    private final List<LatchRotSetting> latches = new ArrayList<>();
    private final BlockPos pos;
    private final int countLocks;
    private int unblockingLatch = 0;
    public int lockPickRot = 0;
    private boolean reverse = false;
    private boolean error = false;

    public LockScreen(BlockPos pos, int countLatches) {
        super(Component.empty());
        this.pos = pos;
        this.countLocks = countLatches;

        Random random = new Random();

        for (int i = 0; i < this.countLocks; i++) {
            if (this.latchesRots.isEmpty()) {
                int rot = random.nextInt(0, 360);

                this.latchesRots.add(new Pair<>(false, rot));
                this.latches.add(new LatchRotSetting(rot));

                continue;
            }

            int randomRot = random.nextInt(0, 361);

            while (!successRot(randomRot)) {
                randomRot = random.nextInt(0, 361);
            }

            this.latchesRots.add(new Pair<>(false, randomRot));
            this.latches.add(new LatchRotSetting(randomRot));
        }
    }

    public void tick() {
        if (this.lockPickRot + (3  * (this.reverse ? -1 : 1)) >= 360)
            this.lockPickRot = this.lockPickRot + (3  * (this.reverse ? -1 : 1)) - 360;
        else if (this.lockPickRot + (3  * (this.reverse ? -1 : 1)) < 0)
            this.lockPickRot = 360 + this.lockPickRot + (3  * (this.reverse ? -1 : 1));
        else
            this.lockPickRot += (3 * (this.reverse ? -1 : 1));
    }

    protected void init() {
        int y = this.height / 2;

        for (int i = 0; i < this.latches.size(); i++) {
            int x = (this.width - getTexSize(MLClientConfig.MENU_TYPE.get().concat(this.latchesRots.get(i).getA() ? "_latch_unblocking" : "_latch_blocking")).getA()) / 2 + 3;

            this.addRenderableWidget(new LatchButton(x, y, i, this));
        }
    }

    public void render(PoseStack stack, int pMouseX, int pMouseY, float pPartialTick) {
        TextureManager manager = this.MC.getTextureManager();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        RenderSystem.setShaderTexture(0, getTexLocation(MLClientConfig.MENU_TYPE.get().concat("_background")));
        manager.bindForSetup(getTexLocation(MLClientConfig.MENU_TYPE.get().concat("_background")));

        Pair<Integer, Integer> backgroundPair = getTexSize(MLClientConfig.MENU_TYPE.get().concat("_background"));

        blit(stack, (this.width - backgroundPair.getA()) / 2, (this.height - backgroundPair.getB())/ 2, 0, 0, backgroundPair.getA(), backgroundPair.getB(), backgroundPair.getA(), backgroundPair.getB());

        super.render(stack, pMouseX, pMouseY, pPartialTick);

        RenderSystem.setShaderTexture(0, getTexLocation(MLClientConfig.MENU_TYPE.get().concat("_lock_pick")));
        manager.bindForSetup(getTexLocation(MLClientConfig.MENU_TYPE.get().concat("_lock_pick")));

        Pair<Integer, Integer> lockPickPair = getTexSize(MLClientConfig.MENU_TYPE.get().concat("_lock_pick"));

        stack.pushPose();
        stack.translate((double) (this.width - lockPickPair.getA()) / 2 + 3.5, (double) this.height / 2 - 0.5, 0);
        stack.mulPose(Vector3f.ZN.rotationDegrees((float) (this.lockPickRot * (this.unblockingLatch == 0 ? 1 : this.unblockingLatch * MLClientConfig.CHANGE_ACCELERATION.get()))));
        stack.translate(-2, -2, 0);

        blit(stack, 0, 0, 0, 0, lockPickPair.getA(), lockPickPair.getB(), lockPickPair.getA(), lockPickPair.getB());

        stack.popPose();
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int clickType) {
        if (clickType == 0) {
            boolean success = false;

            for (int i = 0; i < this.latchesRots.size(); i++) {
                Pair<Boolean, Integer> pair = this.latchesRots.get(i);

                if (pair.getA())
                    continue;

                if (this.latches.get(i).list.contains(this.lockPickRot)) {
                    this.reverse = !this.reverse;
                    success = true;

                    this.latchesRots.set(i, new Pair<>(true, pair.getB()));
                    this.lockPickRot *= MLClientConfig.CHANGE_ACCELERATION.get();

                    this.rebuildWidgets();

                    break;
                }
            }

            if (MathUtils.isRandom(this.MC.level, success ? MLClientConfig.CHANGE_BREAKING_LOCKPICK_SUCCESS.get() : MLClientConfig.CHANGE_BREAKING_LOCKPICK_FAIL.get()))
                Network.sendToServer(new BreakingLockPickPacket());

            if (!hasLockPick()) {
                this.error = true;

                this.onClose();

                return false;
            }

            int unblocking = 0;

            for (Pair<Boolean, Integer> pair : this.latchesRots)
                if (pair.getA())
                    unblocking++;

            if (unblocking == this.latches.size())
                this.onClose();
        }

        return super.mouseClicked(pMouseX, pMouseY, clickType);
    }

    private boolean hasLockPick() {
        Player player = this.MC.player;
        boolean hasItemInContainers = false;

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof LockPickItem && !(stack.getItem() instanceof AutoLockPickItem)) {
                hasItemInContainers = true;

                break;
            }
        }

        if (!hasItemInContainers) {
            for (ItemStack stack : player.getInventory().armor) {
                if (stack.getItem() instanceof LockPickItem && !(stack.getItem() instanceof AutoLockPickItem)) {
                    hasItemInContainers = true;

                    break;
                }
            }
        }

        return (player.getMainHandItem().getItem() instanceof LockPickItem && !(player.getMainHandItem().getItem() instanceof AutoLockPickItem))
                || (player.getOffhandItem().getItem() instanceof LockPickItem && !(player.getOffhandItem().getItem() instanceof AutoLockPickItem)) || hasItemInContainers;
    }

    public void onClose() {
        super.onClose();

        if (!this.error)
            Network.sendToServer(new UnblockingBlockPacket(this.pos));
    }

    public ResourceLocation getTexLocation(String texName) {
        if (MLClientConfig.MENU_TYPE.get().equals("default_0") || MLClientConfig.MENU_TYPE.get().equals("default_1"))
            return new ResourceLocation(MoreLocks.MODID, "textures/gui/" + texName + ".png");
        else
            return ResourceLocationUtils.getImageRL(MoreLocks.MODID, new File("config" + File.separator + MoreLocks.MODID + File.separator + MLClientConfig.MENU_TYPE.get(), texName + ".png"));
    }

    public Pair<Integer, Integer> getTexSize(String texName) {
        if (texName.contains("default_0")) {
            if (texName.contains("_background"))
                return new Pair<>(121, 121);
            else if (texName.contains("_latch_blocking"))
                return new Pair<>(7, 50);
            else if (texName.contains("_latch_unblocking"))
                return new Pair<>(7, 50);
            else
                return new Pair<>(7, 40);
        } else if (texName.contains("default_1")) {
            if (texName.contains("_background"))
                return new Pair<>(126, 126);
            else if (texName.contains("_latch_blocking"))
                return new Pair<>(7, 39);
            else if (texName.contains("_latch_unblocking"))
                return new Pair<>(7, 39);
            else
                return new Pair<>(6, 45);
        } else {
            try {
                BufferedImage image = ImageIO.read(new File("config" + File.separator + MoreLocks.MODID + File.separator + MLClientConfig.MENU_TYPE.get(), texName + ".png"));

                return new Pair<>(image.getWidth(), image.getHeight());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean successRot(int rot) {
        for (LatchRotSetting r : this.latches)
            if (r.list.contains(rot))
                return false;

        return true;
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static class LatchRotSetting {
        private final List<Integer> list;
        private final int rot;

        private LatchRotSetting(int rot) {
            List<Integer> list1 = new ArrayList<>();

            for (int i = 1; i < 5; i++)
                list1.add(rot - i < 0 ? 360 - rot - i : rot - i);

            list1.add(rot);

            for (int i = 1; i < 5; i++)
                list1.add(rot + i >= 360 ? rot + i - 360 : rot + i);

            this.list = list1;
            this.rot = rot;
        }
    }
}
