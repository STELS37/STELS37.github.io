package cc.globalsystem.mlekavoxel.handeditor.config;

public record HandTransform(double x, double y, double z, double scale) {
	public static final double MIN_X = -4.0;
	public static final double MAX_X = 4.0;
	public static final double MIN_Y = -4.0;
	public static final double MAX_Y = 4.0;
	public static final double MIN_Z = -3.0;
	public static final double MAX_Z = 3.0;
	public static final double MIN_SCALE = 0.20;
	public static final double MAX_SCALE = 3.00;

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
		return new HandTransform(x + dx, y + dy, z, scale).sanitized();
	}

	public HandTransform withScale(double newScale) {
		return new HandTransform(x, y, z, newScale).sanitized();
	}

	public HandTransform scaledBy(double factor) {
		return withScale(scale * factor);
	}

	private static double clampFinite(double value, double min, double max, double fallback) {
		if (!Double.isFinite(value)) return fallback;
		return Math.max(min, Math.min(max, value));
	}
}
