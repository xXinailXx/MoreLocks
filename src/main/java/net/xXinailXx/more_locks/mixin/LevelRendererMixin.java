package net.xXinailXx.more_locks.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xXinailXx.enderdragonlib.capability.ServerCapManager;
import net.xXinailXx.more_locks.data.IBlockLocking;
import net.xXinailXx.more_locks.data.LockSetting;
import net.xXinailXx.more_locks.data.LocksData;
import net.xXinailXx.more_locks.item.LockItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import oshi.util.tuples.Pair;

@OnlyIn(Dist.CLIENT)
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Shadow @Final private RenderBuffers renderBuffers;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V", ordinal = 4))
    public void renderBatched(PoseStack stack, float partialTick, long nanoTime, boolean renderOutline, Camera camera, GameRenderer renderer, LightTexture lightTex, Matrix4f matrix4f, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null)
            return;

        MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();

        for (BlockPos pos : LocksData.LOCKABLES.keySet()) {
            Pair<IBlockLocking, ItemStack> pair = LocksData.LOCKABLES.get(pos);
            LockSetting setting = pair.getA().getDefaultCustomLockSetting(mc.level, pos, mc.level.getBlockState(pos), partialTick);

            if (setting == null)
                continue;

            stack.pushPose();

            stack.translate(pos.getX() - camera.getPosition().x + setting.pos().x, pos.getY() - camera.getPosition().y + setting.pos().y, pos.getZ() - camera.getPosition().z + setting.pos().z);
            stack.mulPose(setting.rot());
            stack.scale(0.5F, 0.5F, 0.5F);

            if (!(pair.getB().getItem() instanceof LockItem)) {
                LocksData.LOCKABLES.remove(pos);

                CompoundTag tag = ServerCapManager.getOrCreateData("more_locks_locks_data");

                tag.remove(String.valueOf(pos.asLong()));

                ServerCapManager.addServerData("more_locks_locks_data", tag);

                return;
            }

            mc.getItemRenderer().renderStatic(pair.getB(), ItemTransforms.TransformType.FIXED, 15728880, OverlayTexture.NO_OVERLAY, stack, source, 0);

            stack.popPose();
        }

        source.endBatch();
    }
}
