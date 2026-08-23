package cc.globalsystem.mlekavoxel.handeditor.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwingAnimationType;

/**
 * Additional first-person swing polish layered on top of vanilla animation.
 * The curve always starts and ends at the exact neutral pose, so it cannot accumulate drift.
 */
public final class AttackAnimation {
	private AttackAnimation() {
	}

	public static void apply(PoseStack poseStack, ItemStack stack, HumanoidArm arm, float swingProgress) {
		if (!Float.isFinite(swingProgress) || swingProgress <= 0.0f) {
			return;
		}

		float t = Mth.clamp(swingProgress, 0.0f, 1.0f);
		float hump = Mth.sin(t * (float) Math.PI);
		float strike = 1.0f - (1.0f - hump) * (1.0f - hump);
		float followThrough = Mth.sin(t * (float) Math.PI * 2.0f) * (1.0f - t);
		float side = arm == HumanoidArm.RIGHT ? 1.0f : -1.0f;

		if (stack.isEmpty()) {
			// Empty-hand punch: compact forward snap with a soft wrist follow-through.
			poseStack.translate(-0.045 * side * strike, -0.030 * strike, -0.180 * strike);
			poseStack.mulPose(Axis.XP.rotationDegrees(-18.0f * strike + 5.0f * followThrough));
			poseStack.mulPose(Axis.YP.rotationDegrees(16.0f * side * strike));
			poseStack.mulPose(Axis.ZP.rotationDegrees(-11.0f * side * strike - 4.0f * side * followThrough));
			return;
		}

		SwingAnimationType type = stack.getSwingAnimation().type();
		boolean tool = stack.has(DataComponents.TOOL);

		if (type == SwingAnimationType.STAB) {
			// Swords/items declaring STAB get a clean forward thrust.
			poseStack.translate(-0.020 * side * strike, -0.012 * strike, -0.260 * strike);
			poseStack.mulPose(Axis.XP.rotationDegrees(-10.0f * strike));
			poseStack.mulPose(Axis.YP.rotationDegrees(5.0f * side * strike));
			poseStack.mulPose(Axis.ZP.rotationDegrees(-3.0f * side * strike + 2.0f * side * followThrough));
		} else if (tool) {
			// Tools feel heavier: pronounced downward chop, then a restrained return.
			poseStack.translate(-0.055 * side * strike, -0.075 * strike, 0.030 * strike);
			poseStack.mulPose(Axis.XP.rotationDegrees(-31.0f * strike + 6.0f * followThrough));
			poseStack.mulPose(Axis.YP.rotationDegrees(10.0f * side * strike));
			poseStack.mulPose(Axis.ZP.rotationDegrees(-8.0f * side * strike + 5.0f * side * followThrough));
		} else {
			// General held-item slash: wide arc with a small elastic wrist recovery.
			poseStack.translate(-0.090 * side * strike, -0.025 * strike, -0.045 * strike);
			poseStack.mulPose(Axis.XP.rotationDegrees(-13.0f * strike + 3.0f * followThrough));
			poseStack.mulPose(Axis.YP.rotationDegrees(28.0f * side * strike));
			poseStack.mulPose(Axis.ZP.rotationDegrees(-17.0f * side * strike + 6.0f * side * followThrough));
		}
	}
}
