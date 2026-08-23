package cc.globalsystem.mlekavoxel.handeditor.screen;

import cc.globalsystem.mlekavoxel.handeditor.config.HandEditorConfig;
import cc.globalsystem.mlekavoxel.handeditor.config.HandEditorSettings;
import cc.globalsystem.mlekavoxel.handeditor.config.HandTransform;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.HumanoidArm;

import java.util.Locale;

public final class HandEditorScreen extends Screen {
	private static final double MODEL_UNITS_PER_SCREEN_HEIGHT = 4.0;
	private static final int PANEL_WIDTH = 430;
	private static final int PANEL_HEIGHT = 118;
	private static final int HANDLE_BASE_WIDTH = 92;
	private static final int HANDLE_BASE_HEIGHT = 54;

	private final Screen parent;
	private HumanoidArm selected = HumanoidArm.RIGHT;
	private boolean dragging;
	private boolean dirty;
	private Button rightButton;
	private Button leftButton;

	public HandEditorScreen(Screen parent) {
		super(Component.translatable("screen.mlekavoxel_hand_editor.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int panelX = (this.width - PANEL_WIDTH) / 2;
		int y = 42;
		int gap = 6;
		int handButtonWidth = 116;
		this.rightButton = this.addRenderableWidget(Button.builder(Component.empty(), button -> select(HumanoidArm.RIGHT))
			.bounds(panelX + 14, y, handButtonWidth, 20).build());
		this.leftButton = this.addRenderableWidget(Button.builder(Component.empty(), button -> select(HumanoidArm.LEFT))
			.bounds(panelX + 14 + handButtonWidth + gap, y, handButtonWidth, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("−"), button -> changeScale(1.0 / 1.08))
			.bounds(panelX + 14 + (handButtonWidth + gap) * 2, y, 34, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), button -> changeScale(1.08))
			.bounds(panelX + 14 + (handButtonWidth + gap) * 2 + 40, y, 34, 20).build());
		int y2 = y + 27;
		this.addRenderableWidget(Button.builder(Component.translatable("screen.mlekavoxel_hand_editor.reset_selected"), button -> resetSelected())
			.bounds(panelX + 14, y2, 128, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("screen.mlekavoxel_hand_editor.reset_all"), button -> resetAll())
			.bounds(panelX + 148, y2, 112, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
			.bounds(panelX + PANEL_WIDTH - 104, y2, 90, 20).build());
		updateSelectionLabels();
	}

	private void select(HumanoidArm arm) { this.selected = arm; updateSelectionLabels(); }
	private void updateSelectionLabels() {
		if (rightButton == null || leftButton == null) return;
		rightButton.setMessage(prefix(HumanoidArm.RIGHT).append(Component.translatable("screen.mlekavoxel_hand_editor.right")));
		leftButton.setMessage(prefix(HumanoidArm.LEFT).append(Component.translatable("screen.mlekavoxel_hand_editor.left")));
	}
	private MutableComponent prefix(HumanoidArm arm) { return Component.literal(this.selected == arm ? "● " : "○ "); }
	private void changeScale(double factor) { mutateSelected(transform -> transform.scaledBy(factor)); }
	private void resetSelected() { HandEditorConfig.update(settings -> settings.withArm(this.selected, HandTransform.vanilla())); this.dirty = true; }
	private void resetAll() { HandEditorConfig.set(HandEditorSettings.defaults()); this.dirty = true; }
	private void mutateSelected(java.util.function.UnaryOperator<HandTransform> update) {
		HandEditorConfig.update(settings -> settings.withArm(this.selected, update.apply(settings.forArm(this.selected))));
		this.dirty = true;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int panelX = (this.width - PANEL_WIDTH) / 2;
		graphics.fill(panelX, 10, panelX + PANEL_WIDTH, 10 + PANEL_HEIGHT, 0xB0121826);
		graphics.outline(panelX, 10, PANEL_WIDTH, PANEL_HEIGHT, 0xB86BB7FF);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		Component title = Component.translatable("screen.mlekavoxel_hand_editor.title");
		graphics.text(this.font, title, this.width / 2 - this.font.width(title) / 2, 18, 0xFFFFFFFF, true);
		Component hint = Component.translatable("screen.mlekavoxel_hand_editor.hint");
		graphics.text(this.font, hint, this.width / 2 - this.font.width(hint) / 2, 30, 0xFFD8E8FF, false);
		drawHandle(graphics, HumanoidArm.LEFT);
		drawHandle(graphics, HumanoidArm.RIGHT);
		HandTransform selectedTransform = HandEditorConfig.get().forArm(this.selected);
		String values = String.format(Locale.ROOT, "X %.2f   Y %.2f   Scale %.2fx", selectedTransform.x(), selectedTransform.y(), selectedTransform.scale());
		graphics.text(this.font, values, this.width / 2 - this.font.width(values) / 2, 96, 0xFFFFFFFF, true);
	}

	private void drawHandle(GuiGraphicsExtractor graphics, HumanoidArm arm) {
		HandTransform transform = HandEditorConfig.get().forArm(arm);
		HandleRect rect = handleRect(arm, transform);
		boolean isSelected = arm == this.selected;
		int fill = isSelected ? 0x3D6BB7FF : 0x24161C28;
		int outline = isSelected ? 0xFF79C7FF : 0xAAFFFFFF;
		graphics.fill(rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + rect.height(), fill);
		graphics.outline(rect.x(), rect.y(), rect.width(), rect.height(), outline);
		Component label = Component.translatable(arm == HumanoidArm.RIGHT ? "screen.mlekavoxel_hand_editor.right_handle" : "screen.mlekavoxel_hand_editor.left_handle");
		graphics.text(this.font, label, rect.centerX() - this.font.width(label) / 2, rect.centerY() - 4, 0xFFFFFFFF, true);
	}

	private HandleRect handleRect(HumanoidArm arm, HandTransform transform) {
		double baseX = arm == HumanoidArm.RIGHT ? this.width * 0.72 : this.width * 0.28;
		double baseY = this.height * 0.67;
		double pixelsPerUnit = Math.max(1.0, this.height / MODEL_UNITS_PER_SCREEN_HEIGHT);
		int centerX = (int) Math.round(baseX + transform.x() * pixelsPerUnit);
		int centerY = (int) Math.round(baseY - transform.y() * pixelsPerUnit);
		double visualScale = Math.sqrt(transform.scale());
		int width = (int) Math.round(HANDLE_BASE_WIDTH * visualScale);
		int height = (int) Math.round(HANDLE_BASE_HEIGHT * visualScale);
		return new HandleRect(centerX - width / 2, centerY - height / 2, width, height);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) return true;
		if (event.button() != 0) return false;
		HumanoidArm hit = hitTest(event.x(), event.y());
		if (hit == null) return false;
		select(hit);
		this.dragging = true;
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (!this.dragging || event.button() != 0) return super.mouseDragged(event, dx, dy);
		double pixelsPerUnit = Math.max(1.0, this.height / MODEL_UNITS_PER_SCREEN_HEIGHT);
		mutateSelected(transform -> transform.moved(dx / pixelsPerUnit, -dy / pixelsPerUnit));
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.dragging && event.button() == 0) { this.dragging = false; return true; }
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		if (scrollY != 0.0) { changeScale(Math.pow(1.08, scrollY)); return true; }
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	private HumanoidArm hitTest(double mouseX, double mouseY) {
		HandEditorSettings settings = HandEditorConfig.get();
		HandleRect right = handleRect(HumanoidArm.RIGHT, settings.right()).expanded(8);
		HandleRect left = handleRect(HumanoidArm.LEFT, settings.left()).expanded(8);
		HandleRect selectedRect = this.selected == HumanoidArm.RIGHT ? right : left;
		if (selectedRect.contains(mouseX, mouseY)) return this.selected;
		if (right.contains(mouseX, mouseY)) return HumanoidArm.RIGHT;
		if (left.contains(mouseX, mouseY)) return HumanoidArm.LEFT;
		return null;
	}

	@Override public boolean isPauseScreen() { return false; }
	@Override public void onClose() { persistIfDirty(); if (this.minecraft != null) this.minecraft.setScreen(this.parent); }
	@Override public void removed() { persistIfDirty(); super.removed(); }
	private void persistIfDirty() { if (this.dirty) { HandEditorConfig.save(); this.dirty = false; } }

	private record HandleRect(int x, int y, int width, int height) {
		int centerX() { return x + width / 2; }
		int centerY() { return y + height / 2; }
		boolean contains(double px, double py) { return px >= x && px <= x + width && py >= y && py <= y + height; }
		HandleRect expanded(int amount) { return new HandleRect(x - amount, y - amount, width + amount * 2, height + amount * 2); }
	}
}
