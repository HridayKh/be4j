package in.HridayKh.be4j.di.ReflectionMetas;

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
