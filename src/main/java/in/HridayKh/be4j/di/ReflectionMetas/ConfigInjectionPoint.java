package in.HridayKh.be4j.di.ReflectionMetas;

import java.lang.reflect.Field;

public class ConfigInjectionPoint {
	public final Class<?> ownerClass;
	public final Field field;
	public final String configKey;
	public final String defaultValue;

	public ConfigInjectionPoint(Class<?> ownerClass, Field field, String configKey, String defaultValue) {
		this.ownerClass = ownerClass;
		this.field = field;
		this.configKey = configKey;
		this.defaultValue = defaultValue;
	}
}
