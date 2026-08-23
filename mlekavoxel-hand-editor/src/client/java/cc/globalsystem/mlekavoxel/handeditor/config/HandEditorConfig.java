package cc.globalsystem.mlekavoxel.handeditor.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

public final class HandEditorConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("MlekaVoxel Hand Editor");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final int FORMAT_VERSION = 1;
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("mlekavoxel-hand-editor.json");
	private static final AtomicReference<HandEditorSettings> CURRENT = new AtomicReference<>(HandEditorSettings.defaults());

	private HandEditorConfig() {}

	public static HandEditorSettings get() { return CURRENT.get(); }

	public static void set(HandEditorSettings settings) {
		CURRENT.set((settings == null ? HandEditorSettings.defaults() : settings).sanitized());
	}

	public static void update(UnaryOperator<HandEditorSettings> update) {
		CURRENT.updateAndGet(old -> {
			HandEditorSettings next = update.apply(old);
			return (next == null ? old : next).sanitized();
		});
	}

	public static void load() {
		if (!Files.isRegularFile(CONFIG_PATH)) {
			CURRENT.set(HandEditorSettings.defaults());
			return;
		}
		try {
			DiskConfig disk = GSON.fromJson(Files.readString(CONFIG_PATH, StandardCharsets.UTF_8), DiskConfig.class);
			if (disk == null || disk.version() != FORMAT_VERSION) {
				LOGGER.warn("Unsupported or empty hand-editor config; using safe defaults: {}", CONFIG_PATH);
				CURRENT.set(HandEditorSettings.defaults());
				return;
			}
			CURRENT.set(new HandEditorSettings(disk.right(), disk.left()).sanitized());
		} catch (Exception exception) {
			LOGGER.warn("Could not read hand-editor config; using safe defaults: {}", CONFIG_PATH, exception);
			CURRENT.set(HandEditorSettings.defaults());
		}
	}

	public static synchronized void save() {
		HandEditorSettings snapshot = CURRENT.get().sanitized();
		DiskConfig disk = new DiskConfig(FORMAT_VERSION, snapshot.right(), snapshot.left());
		Path temp = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".tmp");
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(temp, GSON.toJson(disk), StandardCharsets.UTF_8,
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
			try {
				Files.move(temp, CONFIG_PATH, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException ignored) {
				Files.move(temp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException exception) {
			LOGGER.error("Could not save hand-editor config: {}", CONFIG_PATH, exception);
			try { Files.deleteIfExists(temp); } catch (IOException ignored) {}
		}
	}

	private record DiskConfig(int version, HandTransform right, HandTransform left) {}
}
