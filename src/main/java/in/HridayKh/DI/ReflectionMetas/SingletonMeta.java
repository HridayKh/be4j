package in.HridayKh.DI.ReflectionMetas;

public class SingletonMeta {
	public final Class<?> type;
	public final int order;

	// Filled during processing phase
	public Object instance;

	public SingletonMeta(Class<?> type, int order) {
		this.type = type;
		this.order = order;
	}
}
