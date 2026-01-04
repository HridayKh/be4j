package in.HridayKh.be4j.runtime.di;

import java.util.HashMap;
import java.util.Map;

import in.HridayKh.be4j.runtime.config.ConfigLoader;
import in.HridayKh.be4j.runtime.di.ReflectionMetas.ConfigInjectionPoint;
import in.HridayKh.be4j.runtime.di.ReflectionMetas.InjectPoint;
import in.HridayKh.be4j.runtime.di.ReflectionMetas.MethodLevelPathMeta;
import in.HridayKh.be4j.runtime.di.ReflectionMetas.SingletonMeta;

public class RegistryValidator {
	private Registry registry = null;
	private ConfigLoader config = null;

	public RegistryValidator(ConfigLoader config, Registry registry) {
		this.config = config;
		this.registry = registry;
	}

	public void validate() {
		System.out.println("[VALIDATION] Starting registry validation: routes="
				+ registry.methodLevelPaths.size()
				+ ", injectPoints=" + registry.injectPoints.size() + ", configInjectionPoints="
				+ registry.configInjectionPoints.size());

		Map<String, MethodLevelPathMeta> seen = new HashMap<>();
		for (MethodLevelPathMeta route : registry.methodLevelPaths) {
			String key = route.httpMethod + " " + route.fullPath;
			if (seen.containsKey(key)) {
				MethodLevelPathMeta existing = seen.get(key);
				System.err.println("Duplicate route: " + key +
						" defined in both " +
						existing.controllerClass.getName() + "." + existing.method.getName() +
						" and " +
						route.controllerClass.getName() + "." + route.method.getName());
			}
			seen.put(key, route);
		}

		for (InjectPoint ip : registry.injectPoints) {
			Class<?> depType = ip.field.getType();
			if (!registry.singletons.containsKey(depType))
				System.err.println("[VALIDATION] Unsatisfied dependency: " + depType.getName());
		}

		for (ConfigInjectionPoint cip : registry.configInjectionPoints) {
			java.util.Optional<?> opt = config.get(cip.configKey);
			if (!opt.isPresent() || opt.isEmpty())
				System.err.println("[VALIDATION] Missing config key: " + cip.configKey);
		}

		for (Class<?> controllerClass : registry.classLevelPaths.keySet()) {
			ensureSingleton(controllerClass, "class-level @Path but no @Singleton");
		}

		for (MethodLevelPathMeta mlpm : registry.methodLevelPaths) {
			ensureSingleton(mlpm.controllerClass, "method-level @Path but no class-level @Singleton/@Path");
		}

		System.out.println("[VALIDATION] Completed registry validation");

	}

	private void ensureSingleton(Class<?> cls, String reason) {
		if (!registry.singletons.containsKey(cls)) {
			registry.singletons.put(
					cls,
					new SingletonMeta(cls, Integer.MAX_VALUE));

			System.out.println(
					"[VALIDATION] Auto-registered singleton (" + reason + "): " + cls.getName());
		}
	}

}
