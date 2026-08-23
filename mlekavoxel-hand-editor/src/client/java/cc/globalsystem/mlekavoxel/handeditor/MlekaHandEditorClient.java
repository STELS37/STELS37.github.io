package cc.globalsystem.mlekavoxel.handeditor;

import cc.globalsystem.mlekavoxel.handeditor.config.HandEditorConfig;
import cc.globalsystem.mlekavoxel.handeditor.screen.HandEditorScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class MlekaHandEditorClient implements ClientModInitializer {
	public static final String MOD_ID = "mlekavoxel_hand_editor";

	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
		Identifier.fromNamespaceAndPath(MOD_ID, "editor")
	);

	private static final KeyMapping OPEN_EDITOR = KeyMappingHelper.registerKeyMapping(
		new KeyMapping(
			"key.mlekavoxel_hand_editor.open",
			InputConstants.Type.KEYSYM,
			InputConstants.KEY_H,
			CATEGORY
		)
	);

	@Override
	public void onInitializeClient() {
		HandEditorConfig.load();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_EDITOR.consumeClick()) {
				if (client.player != null && client.screen == null) {
					client.setScreen(new HandEditorScreen(null));
				}
			}
		});
	}
}
