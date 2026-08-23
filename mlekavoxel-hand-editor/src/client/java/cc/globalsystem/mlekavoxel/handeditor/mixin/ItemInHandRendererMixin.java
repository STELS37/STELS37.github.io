package cc.globalsystem.mlekavoxel.handeditor.mixin;

import cc.globalsystem.mlekavoxel.handeditor.config.HandEditorConfig;
import cc.globalsystem.mlekavoxel.handeditor.config.HandTransform;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	@Inject(method = "renderArmWithItem", at = @At("HEAD"))
	private void mlekavoxel$pushHandTransform(AbstractClientPlayer player, float partialTick, float xRot,
		InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
		poseStack.pushPose();
		HumanoidArm physicalArm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
		HandTransform transform = HandEditorConfig.get().forArm(physicalArm);
		poseStack.translate(transform.x(), transform.y(), transform.z());
		float scale = (float) transform.scale();
		poseStack.scale(scale, scale, scale);
	}

	@Inject(method = "renderArmWithItem", at = @At("RETURN"))
	private void mlekavoxel$popHandTransform(AbstractClientPlayer player, float partialTick, float xRot,
		InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
		poseStack.popPose();
	}
}
