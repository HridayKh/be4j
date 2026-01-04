package in.HridayKh.be4j.di;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.HridayKh.be4j.di.ReflectionMetas.ConfigInjectionPoint;
import in.HridayKh.be4j.di.ReflectionMetas.InjectPoint;
import in.HridayKh.be4j.di.ReflectionMetas.MethodLevelPathMeta;
import in.HridayKh.be4j.di.ReflectionMetas.SingletonMeta;

public class Registry {

	/* ================= CONFIG ================= */

	// All @Config injection points (field-level)
	public final List<ConfigInjectionPoint> configInjectionPoints = new ArrayList<>();

	/* ================= DI ================= */

	// All singleton classes discovered (class-level)
	public final Map<Class<?>, SingletonMeta> singletons = new HashMap<>();

	// All @Inject field locations (field-level)
	public final List<InjectPoint> injectPoints = new ArrayList<>();

	/* ================= HTTP ================= */

	// Base paths on classes (class-level)
	public final Map<Class<?>, String> classLevelPaths = new HashMap<>();

	// All HTTP routes (method-level)
	public final List<MethodLevelPathMeta> methodLevelPaths = new ArrayList<>();

}
