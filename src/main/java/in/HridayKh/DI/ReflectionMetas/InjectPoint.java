package in.HridayKh.DI.ReflectionMetas;

import java.lang.reflect.Field;

public class InjectPoint {
	public final Class<?> ownerClass;
	public final Field field;

	public InjectPoint(Class<?> ownerClass, Field field) {
		this.ownerClass = ownerClass;
		this.field = field;
	}
}
