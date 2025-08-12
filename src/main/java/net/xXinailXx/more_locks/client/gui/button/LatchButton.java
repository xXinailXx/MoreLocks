package net.xXinailXx.more_locks.client.gui.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.texture.TextureManager;
import net.xXinailXx.enderdragonlib.client.utils.gui.AbstractWidget;
import net.xXinailXx.more_locks.client.gui.LockScreen;
import net.xXinailXx.more_locks.config.MLClientConfig;
import oshi.util.tuples.Pair;

public class LatchButton extends AbstractWidget {
    private final LockScreen screen;
    private final int latchAmount;

    public LatchButton(int x, int y, int latchAmount, LockScreen screen) {
        super(x, y, 0, 0);
        this.screen = screen;
        this.latchAmount = latchAmount;
    }

    public void renderButton(PoseStack stack, int pMouseX, int pMouseY, float pPartialTick) {
        Pair<Boolean, Integer> pair = this.screen.getLatchesRots().get(this.latchAmount);

        TextureManager manager = this.MC.getTextureManager();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        Pair<Integer, Integer> texSize = this.screen.getTexSize(MLClientConfig.MENU_TYPE.get().concat(pair.getA() ? "_latch_unblocking" : "_latch_blocking"));

        if (!pair.getA()) {
            RenderSystem.setShaderTexture(0, this.screen.getTexLocation(MLClientConfig.MENU_TYPE.get().concat("_latch_blocking")));
            manager.bindForSetup(this.screen.getTexLocation(MLClientConfig.MENU_TYPE.get().concat("_latch_blocking")));
        } else {
            RenderSystem.setShaderTexture(0, this.screen.getTexLocation(MLClientConfig.MENU_TYPE.get().concat("_latch_unblocking")));
            manager.bindForSetup(this.screen.getTexLocation(MLClientConfig.MENU_TYPE.get().concat("_latch_unblocking")));
        }

        stack.pushPose();
        stack.translate(this.x + 0.5, this.y - 0.5, 0);
        stack.mulPose(Vector3f.ZN.rotationDegrees(this.screen.getLatchesRots().get(this.latchAmount).getB()));
        stack.translate(-4, 4, 0);

        blit(stack, 0, 0, 0, 0, texSize.getA(), texSize.getB(), texSize.getA(), texSize.getB());

        stack.popPose();
    }
}
