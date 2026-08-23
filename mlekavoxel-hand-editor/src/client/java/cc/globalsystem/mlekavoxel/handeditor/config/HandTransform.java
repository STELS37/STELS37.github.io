package cc.globalsystem.mlekavoxel.handeditor.config;

/** Immutable first-person transform for one physical arm. */
public record HandTransform(double x, double y, double z, double scale) {
	public static final double MIN_X = -64.0;
	public static final double MAX_X = 64.0;
	public static final double MIN_Y = -64.0;
	public static final double MAX_Y = 64.0;
	public static final double MIN_Z = -64.0;
	public static final double MAX_Z = 64.0;

	// Practically unrestricted for first-person rendering while guaranteeing a strictly non-zero matrix.
	// 0.0001% .. 100000% is far wider than a player can sensibly use, but remains finite and stable.
	public static final double MIN_SCALE = 0.000001;
	public static final double MAX_SCALE = 1000.0;

	public static HandTransform vanilla() {
		return new HandTransform(0.0, 0.0, 0.0, 1.0);
	}

	public HandTransform sanitized() {
		return new HandTransform(
			clampFinite(x, MIN_X, MAX_X, 0.0),
			clampFinite(y, MIN_Y, MAX_Y, 0.0),
			clampFinite(z, MIN_Z, MAX_Z, 0.0),
			clampFinite(scale, MIN_SCALE, MAX_SCALE, 1.0)
		);
	}

	public HandTransform moved(double dx, double dy) {
		return moved(dx, dy, 0.0);
	}

	public HandTransform moved(double dx, double dy, double dz) {
		return new HandTransform(x + dx, y + dy, z + dz, scale).sanitized();
	}

	public HandTransform withX(double newX) {
		return new HandTransform(newX, y, z, scale).sanitized();
	}

	public HandTransform withY(double newY) {
		return new HandTransform(x, newY, z, scale).sanitized();
	}

	public HandTransform withZ(double newZ) {
		return new HandTransform(x, y, newZ, scale).sanitized();
	}

	public HandTransform withScale(double newScale) {
		return new HandTransform(x, y, z, newScale).sanitized();
	}

	public HandTransform scaledBy(double factor) {
		if (!Double.isFinite(factor) || factor <= 0.0) {
			return this;
		}
		return withScale(scale * factor);
	}

	private static double clampFinite(double value, double min, double max, double fallback) {
		if (!Double.isFinite(value)) {
			return fallback;
		}
		return Math.max(min, Math.min(max, value));
	}
}
