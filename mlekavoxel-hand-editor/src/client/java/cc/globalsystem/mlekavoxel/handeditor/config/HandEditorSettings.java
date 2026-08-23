package cc.globalsystem.mlekavoxel.handeditor.config;

import net.minecraft.world.entity.HumanoidArm;

public record HandEditorSettings(HandTransform right, HandTransform left) {
	public static HandEditorSettings defaults() {
		return new HandEditorSettings(HandTransform.vanilla(), HandTransform.vanilla());
	}

	public HandEditorSettings sanitized() {
		return new HandEditorSettings(
			right == null ? HandTransform.vanilla() : right.sanitized(),
			left == null ? HandTransform.vanilla() : left.sanitized()
		);
	}

	public HandTransform forArm(HumanoidArm arm) {
		return arm == HumanoidArm.LEFT ? left : right;
	}

	public HandEditorSettings withArm(HumanoidArm arm, HandTransform transform) {
		HandTransform safe = transform == null ? HandTransform.vanilla() : transform.sanitized();
		return arm == HumanoidArm.LEFT
			? new HandEditorSettings(right, safe)
			: new HandEditorSettings(safe, left);
	}
}
