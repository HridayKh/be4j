package in.HridayKh.be4j.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Optional;

/**
 * Loads application configuration from the classpath and provides convenient
 * accessor methods.
 *
 * Why this class exists:
 * - Centralize access to application properties so callers don't need to
 * repeatedly load files or parse values.
 * - Use a simple singleton to avoid repeated I/O and keep a single shared
 * copy of configuration across the application lifecycle.
 */
public class ConfigLoader {

	/**
	 * In-memory storage of loaded properties.
	 *
	 * Why: using java.util.Properties is a simple, well-known representation
	 * for key/value configuration and fits the needs of a small HTTP server.
	 * Keeping them in-memory avoids re-reading the file for each lookup.
	 */
	private final Properties props = new Properties();

	private String DEFAULT_RESOURCE;

	/**
	 * Private constructor so callers must use {@link #getInstance()}.
	 *
	 * Why: enforcing a single instance reduces accidental multiple loads and
	 * ensures consistent configuration throughout the process.
	 */
	public ConfigLoader(String resource) {
		this.DEFAULT_RESOURCE = resource;
	}

	/**
	 * Load properties from the classpath resource.
	 *
	 * Why: using the classloader lets the application load properties from a
	 * packaged JAR or from the classpath during development. IO exceptions
	 * are intentionally ignored because absence of a properties file should
	 * not crash the application; callers can rely on defaults instead.
	 */
	public void load() {
		props.clear();
		try (InputStream in = getClass().getClassLoader().getResourceAsStream(DEFAULT_RESOURCE)) {
			if (in != null)
				props.load(in);

		} catch (IOException ignored) {
			// Ignore: treat as no properties loaded. Why: keep startup resilient
			// and allow the app to run with built-in defaults.
		}
	}

	/**
	 * Get a property as an Optional.
	 *
	 * Why Optional: callers may want to explicitly handle absence of the
	 * property rather than relying on magic defaults; Optional makes the
	 * contract explicit.
	 */
	public Optional<String> get(String key) {
		return Optional.ofNullable(props.getProperty(key));
	}

	/**
	 * Get a property with a default value.
	 *
	 * Why: convenience wrapper over {@link Properties#getProperty} to keep
	 * calling code succinct and consistent about defaults.
	 */
	public String get(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	/**
	 * Parse an integer property, returning a default if missing or invalid.
	 *
	 * Why: properties are strings; providing a typed accessor centralizes
	 * parsing and defaulting logic so callers don't duplicate it and so
	 * malformed values are handled gracefully.
	 */
	public int getInt(String key, int defaultValue) {
		String v = props.getProperty(key);
		if (v == null)
			return defaultValue;
		try {
			return Integer.parseInt(v.trim());
		} catch (NumberFormatException e) {
			// Why fallback silently: configuration parsing errors should not
			// cause runtime failures; use the provided default instead.
			return defaultValue;
		}
	}

	/**
	 * Parse a boolean property, returning a default if missing.
	 *
	 * Why: centralize boolean parsing and trimming to avoid subtle bugs from
	 * whitespace or different string representations across the codebase.
	 */
	public boolean getBoolean(String key, boolean defaultValue) {
		String v = props.getProperty(key);
		return v != null ? Boolean.parseBoolean(v.trim()) : defaultValue;
	}

	/**
	 * Return a shallow copy of the loaded properties.
	 *
	 * Why a copy: protect internal state so callers can't accidentally
	 * mutate the shared properties instance.
	 */
	public Properties asProperties() {
		Properties copy = new Properties();
		copy.putAll(this.props);
		return copy;
	}
}
