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
import java.util.function.UnaryOperator;

/** Live non-pausing editor for physical right/left first-person arms. */
public final class HandEditorScreen extends Screen {
	private static final double MODEL_UNITS_PER_SCREEN_HEIGHT = 4.0;
	private static final int PANEL_MAX_WIDTH = 520;
	private static final int PANEL_HEIGHT = 252;
	private static final int HANDLE_BASE_WIDTH = 92;
	private static final int HANDLE_BASE_HEIGHT = 54;
	private static final double POSITION_STEP = 0.05;
	private static final double SCALE_STEP = 1.12;

	private final Screen parent;
	private HumanoidArm selected = HumanoidArm.RIGHT;
	private int dragButton = -1;
	private boolean dirty;
	private Button rightButton;
	private Button leftButton;

	public HandEditorScreen(Screen parent) {
		super(Component.translatable("screen.mlekavoxel_hand_editor.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int x = panelX();
		int w = panelWidth();
		int selectorY = 50;
		int selectorGap = 6;
		int selectorWidth = (w - 38) / 2;

		this.rightButton = this.addRenderableWidget(Button.builder(Component.empty(), button -> select(HumanoidArm.RIGHT))
			.bounds(x + 16, selectorY, selectorWidth, 22).build());
		this.leftButton = this.addRenderableWidget(Button.builder(Component.empty(), button -> select(HumanoidArm.LEFT))
			.bounds(x + 22 + selectorWidth, selectorY, selectorWidth, 22).build());

		addAxisButtons(x, w, 84, () -> nudge(-POSITION_STEP, 0.0, 0.0), () -> nudge(POSITION_STEP, 0.0, 0.0));
		addAxisButtons(x, w, 109, () -> nudge(0.0, -POSITION_STEP, 0.0), () -> nudge(0.0, POSITION_STEP, 0.0));
		addAxisButtons(x, w, 134, () -> nudge(0.0, 0.0, -POSITION_STEP), () -> nudge(0.0, 0.0, POSITION_STEP));
		addAxisButtons(x, w, 159, () -> changeScale(1.0 / SCALE_STEP), () -> changeScale(SCALE_STEP));

		int presetsY = 190;
		String[] names = {"10%", "25%", "50%", "100%", "200%", "500%"};
		double[] scales = {0.10, 0.25, 0.50, 1.0, 2.0, 5.0};
		int gap = 4;
		int presetWidth = Math.max(42, (w - 32 - gap * (names.length - 1)) / names.length);
		for (int i = 0; i < names.length; i++) {
			int index = i;
			this.addRenderableWidget(Button.builder(Component.literal(names[i]), button -> setScale(scales[index]))
				.bounds(x + 16 + i * (presetWidth + gap), presetsY, presetWidth, 20).build());
		}

		int bottomY = 220;
		this.addRenderableWidget(Button.builder(Component.translatable("screen.mlekavoxel_hand_editor.reset_selected"), button -> resetSelected())
			.bounds(x + 16, bottomY, 124, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("screen.mlekavoxel_hand_editor.reset_all"), button -> resetAll())
			.bounds(x + 146, bottomY, 106, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
			.bounds(x + w - 106, bottomY, 90, 20).build());

		updateSelectionLabels();
	}

	private void addAxisButtons(int panelX, int panelWidth, int y, Runnable minus, Runnable plus) {
		this.addRenderableWidget(Button.builder(Component.literal("−"), button -> minus.run())
			.bounds(panelX + panelWidth - 82, y, 30, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), button -> plus.run())
			.bounds(panelX + panelWidth - 46, y, 30, 20).build());
	}

	private int panelWidth() {
		return Math.min(PANEL_MAX_WIDTH, Math.max(380, this.width - 24));
	}

	private int panelX() {
		return (this.width - panelWidth()) / 2;
	}

	private void select(HumanoidArm arm) {
		this.selected = arm;
		updateSelectionLabels();
	}

	private void updateSelectionLabels() {
		if (rightButton == null || leftButton == null) {
			return;
		}
		rightButton.setMessage(prefix(HumanoidArm.RIGHT).append(Component.translatable("screen.mlekavoxel_hand_editor.right")));
		leftButton.setMessage(prefix(HumanoidArm.LEFT).append(Component.translatable("screen.mlekavoxel_hand_editor.left")));
	}

	private MutableComponent prefix(HumanoidArm arm) {
		return Component.literal(this.selected == arm ? "◆ " : "◇ ");
	}

	private void nudge(double dx, double dy, double dz) {
		mutateSelected(transform -> transform.moved(dx, dy, dz));
	}

	private void changeScale(double factor) {
		mutateSelected(transform -> transform.scaledBy(factor));
	}

	private void setScale(double scale) {
		mutateSelected(transform -> transform.withScale(scale));
	}

	private void resetSelected() {
		HandEditorConfig.update(settings -> settings.withArm(this.selected, HandTransform.vanilla()));
		this.dirty = true;
	}

	private void resetAll() {
		HandEditorConfig.set(HandEditorSettings.defaults());
		this.dirty = true;
	}

	private void mutateSelected(UnaryOperator<HandTransform> update) {
		HandEditorConfig.update(settings -> settings.withArm(this.selected, update.apply(settings.forArm(this.selected))));
		this.dirty = true;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int x = panelX();
		int w = panelWidth();
		int bottom = 10 + PANEL_HEIGHT;

		// Semi-transparent winter glass: the real first-person hand remains visible behind the editor.
		graphics.fillGradient(0, 0, this.width, this.height, 0x65040A16, 0x8A06152B);
		drawSnow(graphics);
		graphics.fillGradient(x, 10, x + w, bottom, 0xE8172947, 0xE908162A);
		graphics.outline(x, 10, w, PANEL_HEIGHT, 0xD66FCBFF);
		graphics.outline(x + 2, 12, w - 4, PANEL_HEIGHT - 4, 0x553A78A8);

		long scan = (System.currentTimeMillis() / 18L) % Math.max(1, PANEL_HEIGHT - 4);
		graphics.fill(x + 3, 12 + (int) scan, x + w - 3, 13 + (int) scan, 0x182CB8FF);

		super.extractRenderState(graphics, mouseX, mouseY, delta);

		Component title = Component.translatable("screen.mlekavoxel_hand_editor.title");
		graphics.text(this.font, title, x + 16, 19, 0xFFFFFFFF, true);
		graphics.text(this.font, Component.translatable("screen.mlekavoxel_hand_editor.live"), x + w - 122, 19, 0xFF7DE3FF, true);
		graphics.text(this.font, Component.translatable("screen.mlekavoxel_hand_editor.hint"), x + 16, 33, 0xFFC7DBF5, false);

		HandTransform transform = HandEditorConfig.get().forArm(this.selected);
		drawAxisRow(graphics, x, w, 84, "X", transform.x(), axisNormalized(transform.x()), "screen.mlekavoxel_hand_editor.axis_x");
		drawAxisRow(graphics, x, w, 109, "Y", transform.y(), axisNormalized(transform.y()), "screen.mlekavoxel_hand_editor.axis_y");
		drawAxisRow(graphics, x, w, 134, "Z", transform.z(), axisNormalized(transform.z()), "screen.mlekavoxel_hand_editor.axis_z");
		drawScaleRow(graphics, x, w, 159, transform.scale());

		graphics.text(this.font, Component.translatable("screen.mlekavoxel_hand_editor.presets"), x + 16, 178, 0xFF86D8FF, false);
		graphics.text(this.font, Component.translatable("screen.mlekavoxel_hand_editor.attack"), x + w - 164, 178, 0xFFBDEBFF, true);

		drawHandle(graphics, HumanoidArm.LEFT);
		drawHandle(graphics, HumanoidArm.RIGHT);
	}

	private void drawAxisRow(GuiGraphicsExtractor graphics, int x, int w, int y, String axis, double value, double normalized, String labelKey) {
		graphics.text(this.font, axis, x + 18, y + 6, 0xFF79D8FF, true);
		graphics.text(this.font, Component.translatable(labelKey), x + 38, y + 6, 0xFFDCEBFA, false);
		String number = String.format(Locale.ROOT, "%+.3f", value);
		graphics.text(this.font, number, x + 178, y + 6, 0xFFFFFFFF, true);
		drawTrack(graphics, x + 245, x + w - 94, y + 9, normalized);
	}

	private void drawScaleRow(GuiGraphicsExtractor graphics, int x, int w, int y, double scale) {
		graphics.text(this.font, "S", x + 18, y + 6, 0xFF79D8FF, true);
		graphics.text(this.font, Component.translatable("screen.mlekavoxel_hand_editor.scale"), x + 38, y + 6, 0xFFDCEBFA, false);
		String number = scale >= 0.01
			? String.format(Locale.ROOT, "%.2fx", scale)
			: String.format(Locale.ROOT, "%.6fx", scale);
		graphics.text(this.font, number, x + 178, y + 6, 0xFFFFFFFF, true);
		double normalized = (Math.log10(scale) - Math.log10(HandTransform.MIN_SCALE))
			/ (Math.log10(HandTransform.MAX_SCALE) - Math.log10(HandTransform.MIN_SCALE));
		drawTrack(graphics, x + 245, x + w - 94, y + 9, clamp01(normalized));
	}

	private void drawTrack(GuiGraphicsExtractor graphics, int startX, int endX, int y, double normalized) {
		if (endX <= startX) {
			return;
		}
		graphics.fill(startX, y, endX, y + 3, 0x553E6F94);
		int markerX = startX + (int) Math.round((endX - startX) * clamp01(normalized));
		graphics.fill(markerX - 1, y - 3, markerX + 2, y + 6, 0xFF79D8FF);
	}

	private double axisNormalized(double value) {
		return 0.5 + Math.tanh(value / 3.0) * 0.5;
	}

	private double clamp01(double value) {
		return Math.max(0.0, Math.min(1.0, value));
	}

	private void drawSnow(GuiGraphicsExtractor graphics) {
		long tick = System.currentTimeMillis() / 34L;
		for (int i = 0; i < 24; i++) {
			int sx = Math.floorMod(i * 97 + (int) (tick * (1 + i % 3)), Math.max(1, this.width));
			int sy = Math.floorMod(i * 53 + (int) (tick * (2 + i % 2)), Math.max(1, this.height));
			int size = i % 7 == 0 ? 2 : 1;
			int color = i % 3 == 0 ? 0x99DFF7FF : 0x6677CFFF;
			graphics.fill(sx, sy, sx + size, sy + size, color);
		}
	}

	private void drawHandle(GuiGraphicsExtractor graphics, HumanoidArm arm) {
		HandTransform transform = HandEditorConfig.get().forArm(arm);
		HandleRect rect = handleRect(arm, transform);
		boolean isSelected = arm == this.selected;
		int fill = isSelected ? 0x5064C7FF : 0x2A101A2A;
		int outline = isSelected ? 0xFF82DEFF : 0x99BFD6E8;
		graphics.fill(rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + rect.height(), fill);
		graphics.outline(rect.x(), rect.y(), rect.width(), rect.height(), outline);
		graphics.fill(rect.centerX() - 7, rect.centerY(), rect.centerX() + 8, rect.centerY() + 1, 0xBBFFFFFF);
		graphics.fill(rect.centerX(), rect.centerY() - 7, rect.centerX() + 1, rect.centerY() + 8, 0xBBFFFFFF);

		Component label = Component.translatable(arm == HumanoidArm.RIGHT
			? "screen.mlekavoxel_hand_editor.right_handle"
			: "screen.mlekavoxel_hand_editor.left_handle");
		graphics.text(this.font, label, rect.centerX() - this.font.width(label) / 2, rect.y() + 5, 0xFFFFFFFF, true);

		String z = String.format(Locale.ROOT, "Z %+.2f", transform.z());
		graphics.text(this.font, z, rect.centerX() - this.font.width(z) / 2, rect.y() + rect.height() - 12, 0xFFBFEAFF, false);
	}

	private HandleRect handleRect(HumanoidArm arm, HandTransform transform) {
		double baseX = arm == HumanoidArm.RIGHT ? this.width * 0.72 : this.width * 0.28;
		int panelBottom = 10 + PANEL_HEIGHT;
		double baseY = Math.min(this.height - 42.0, Math.max(panelBottom + 58.0, this.height * 0.72));
		double pixelsPerUnit = Math.max(1.0, this.height / MODEL_UNITS_PER_SCREEN_HEIGHT);
		int centerX = (int) Math.round(baseX + transform.x() * pixelsPerUnit);
		int centerY = (int) Math.round(baseY - transform.y() * pixelsPerUnit);

		// Keep the editor handle clickable even when the real hand is microscopic.
		double previewScale = Math.max(0.20, Math.min(3.0, transform.scale()));
		int width = Math.max(34, (int) Math.round(HANDLE_BASE_WIDTH * previewScale));
		int height = Math.max(24, (int) Math.round(HANDLE_BASE_HEIGHT * previewScale));
		return new HandleRect(centerX - width / 2, centerY - height / 2, width, height);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (event.button() != 0 && event.button() != 1) {
			return false;
		}

		HumanoidArm hit = hitTest(event.x(), event.y());
		if (hit == null) {
			return false;
		}
		select(hit);
		this.dragButton = event.button();
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (this.dragButton < 0 || event.button() != this.dragButton) {
			return super.mouseDragged(event, dx, dy);
		}

		double pixelsPerUnit = Math.max(1.0, this.height / MODEL_UNITS_PER_SCREEN_HEIGHT);
		if (this.dragButton == 0) {
			// LMB: true screen-plane movement on X/Y.
			mutateSelected(transform -> transform.moved(dx / pixelsPerUnit, -dy / pixelsPerUnit, 0.0));
		} else {
			// RMB: depth movement. Drag up to move forward, down to move backward.
			mutateSelected(transform -> transform.moved(0.0, 0.0, -dy / pixelsPerUnit));
		}
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.dragButton >= 0 && event.button() == this.dragButton) {
			this.dragButton = -1;
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		if (scrollY != 0.0) {
			changeScale(Math.pow(SCALE_STEP, scrollY));
			return true;
		}
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	private HumanoidArm hitTest(double mouseX, double mouseY) {
		HandEditorSettings settings = HandEditorConfig.get();
		HandleRect right = handleRect(HumanoidArm.RIGHT, settings.right()).expanded(8);
		HandleRect left = handleRect(HumanoidArm.LEFT, settings.left()).expanded(8);

		HandleRect selectedRect = this.selected == HumanoidArm.RIGHT ? right : left;
		if (selectedRect.contains(mouseX, mouseY)) {
			return this.selected;
		}
		if (right.contains(mouseX, mouseY)) {
			return HumanoidArm.RIGHT;
		}
		if (left.contains(mouseX, mouseY)) {
			return HumanoidArm.LEFT;
		}
		return null;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		persistIfDirty();
		if (this.minecraft != null) {
			this.minecraft.setScreen(this.parent);
		}
	}

	@Override
	public void removed() {
		persistIfDirty();
		super.removed();
	}

	private void persistIfDirty() {
		if (this.dirty) {
			HandEditorConfig.save();
			this.dirty = false;
		}
	}

	private record HandleRect(int x, int y, int width, int height) {
		int centerX() {
			return x + width / 2;
		}

		int centerY() {
			return y + height / 2;
		}

		boolean contains(double px, double py) {
			return px >= x && px <= x + width && py >= y && py <= y + height;
		}

		HandleRect expanded(int amount) {
			return new HandleRect(x - amount, y - amount, width + amount * 2, height + amount * 2);
		}
	}
}
