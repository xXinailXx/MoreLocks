package net.xXinailXx.more_locks.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.xXinailXx.enderdragonlib.utils.MathUtils;
import net.xXinailXx.enderdragonlib.utils.FileUtils;
import net.xXinailXx.more_locks.MoreLocks;
import net.xXinailXx.more_locks.client.gui.button.LatchButton;
import net.xXinailXx.more_locks.config.MLClientConfig;
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
    private float lockPickRot = 0;
    private int unblockingLatch = 0;
    private boolean reverse = false;
    private boolean unblocking = false;

    public LockScreen(BlockPos pos, int countLatches) {
        super(Component.empty());
        this.pos = pos;

        Random random = new Random();

        for (int i = 0; i < countLatches; i++) {
            if (this.latchesRots.isEmpty()) {
                int rot = random.nextInt(0, 360);

                this.latchesRots.add(new Pair<>(false, rot));
                this.latches.add(new LatchRotSetting(rot));

                continue;
            }

            int randomRot = random.nextInt(0, 360);

            while (!successRot(new LatchRotSetting(randomRot))) {
                randomRot = random.nextInt(0, 360);
            }

            this.latchesRots.add(new Pair<>(false, randomRot));
            this.latches.add(new LatchRotSetting(randomRot));
        }
    }

    public void tick() {
        if (!hasLockPick()) {
            this.onClose();

            return;
        }

        float extraRot = ((this.reverse ? -1 : 1) * MLClientConfig.CHANGE_BASE_ACCELERATION.get());

        if (this.lockPickRot + extraRot >= 360)
            this.lockPickRot = this.lockPickRot + extraRot - 360;
        else if (this.lockPickRot + extraRot < 0)
            this.lockPickRot = 360 + this.lockPickRot + extraRot;
        else
            this.lockPickRot += (3 * (this.reverse ? -1 : 1));
    }

    protected void init() {
        int x = this.width / 2;
        int y = this.height / 2;

        for (int i = 0; i < this.latches.size(); i++)
            this.addRenderableWidget(new LatchButton(x, y, i, this));
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
        stack.translate((double) this.width / 2, (double) this.height / 2 - 0.5, 0);
        stack.mulPose(Vector3f.ZN.rotationDegrees(this.lockPickRot));
        stack.translate((double) - lockPickPair.getA() / 2, -2, 0);

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

                if (this.latches.get(i).list.contains((int) this.lockPickRot)) {
                    this.reverse = !this.reverse;
                    success = true;
                    this.unblockingLatch++;

                    this.latchesRots.set(i, new Pair<>(true, pair.getB()));

                    this.rebuildWidgets();

                    break;
                }
            }

            if (MathUtils.isRandom(this.MC.level, success ? MLClientConfig.CHANGE_BREAKING_LOCKPICK_SUCCESS.get() : MLClientConfig.CHANGE_BREAKING_LOCKPICK_FAIL.get()))
                Network.sendToServer(new BreakingLockPickPacket());

            int unblocking = 0;

            for (Pair<Boolean, Integer> pair : this.latchesRots)
                if (pair.getA())
                    unblocking++;

            if (unblocking == this.latches.size()) {
                this.unblocking = true;

                this.onClose();
            }
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

        if (this.unblocking)
            Network.sendToServer(new UnblockingBlockPacket(this.pos));
    }

    public ResourceLocation getTexLocation(String texName) {
        if (MLClientConfig.MENU_TYPE.get().equals("default_0") || MLClientConfig.MENU_TYPE.get().equals("default_1"))
            return new ResourceLocation(MoreLocks.MODID, "textures/gui/" + texName + ".png");
        else
            return FileUtils.getImageRL(MoreLocks.MODID, new File("config" + File.separator + MoreLocks.MODID + File.separator + MLClientConfig.MENU_TYPE.get(), texName + ".png"));
    }

    public Pair<Integer, Integer> getTexSize(String texName) {
        if (texName.contains("default_0")) {
            if (texName.contains("_background"))
                return new Pair<>(131, 131);
            else if (texName.contains("_latch_blocking"))
                return new Pair<>(7, 58);
            else if (texName.contains("_latch_unblocking"))
                return new Pair<>(7, 58);
            else
                return new Pair<>(9, 47);
        } else if (texName.contains("default_1")) {
            if (texName.contains("_background"))
                return new Pair<>(126, 126);
            else if (texName.contains("_latch_blocking"))
                return new Pair<>(4, 50);
            else if (texName.contains("_latch_unblocking"))
                return new Pair<>(4, 50);
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

    private boolean successRot(LatchRotSetting tempRot) {
        for (LatchRotSetting r : this.latches)
            for (int rot : r.getList())
                if (tempRot.getList().contains(rot))
                    return false;

        return true;
    }

    public boolean isPauseScreen() {
        return false;
    }

    @Getter
    private static class LatchRotSetting {
        private final List<Integer> list;

        private LatchRotSetting(int rot) {
            List<Integer> list1 = new ArrayList<>();

            for (int i = 1; i < 6; i++)
                list1.add(rot - i < 0 ? 360 - (rot - i) : rot - i);

            list1.add(rot);

            for (int i = 1; i < 6; i++)
                list1.add(rot + i >= 360 ? rot + i - 360 : rot + i);

            this.list = list1;
        }
    }
}
