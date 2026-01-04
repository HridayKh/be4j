package in.HridayKh.be4j.di;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;

import in.HridayKh.be4j.config.ConfigLoader;
import in.HridayKh.be4j.di.ReflectionMetas.ConfigInjectionPoint;
import in.HridayKh.be4j.di.ReflectionMetas.InjectPoint;
import in.HridayKh.be4j.di.ReflectionMetas.SingletonMeta;

public class DiContainer {
	private Registry registry = null;
	private ConfigLoader config = null;

	public DiContainer(ConfigLoader configLoader, Registry registry) {
		this.registry = registry;
		this.config = configLoader;
	}

	public void injectConfigs() {
		for (ConfigInjectionPoint cip : registry.configInjectionPoints) {
			Object owner = getSingletonInstance(cip.ownerClass);
			if (owner == null)
				continue;

			String value = config.get(cip.configKey, cip.defaultValue);

			cip.field.setAccessible(true);
			try {
				cip.field.set(owner, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				System.err.println("[CONFIG INJECTION] Failed to inject config value for key '"
						+ cip.configKey + "' into field '"
						+ cip.field.getName() + "' of class '"
						+ cip.ownerClass.getName() + "': "
						+ e.getMessage());
				e.printStackTrace();
			}
		}

	}

	public void injectSingletons() {
		// instantiate singletons
		registry.singletons.values().stream()
				.sorted(Comparator.comparingInt(m -> m.order))
				.forEach(meta -> {
					try {
						meta.instance = meta.type.getDeclaredConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException
							| IllegalArgumentException | InvocationTargetException
							| NoSuchMethodException | SecurityException e) {
						System.err.println(
								"[SINGLETON INSTANTIATION] Failed to instantiate singleton of type '"
										+ meta.type.getName() + "': "
										+ e.getMessage());
						e.printStackTrace();
					}
				});

		// inject singletons
		for (InjectPoint ip : registry.injectPoints) {
			Object owner = getSingletonInstance(ip.ownerClass);
			Object dependency = getSingletonInstance(ip.field.getType());

			if (owner == null || dependency == null) {
				System.err.println("[DI] Injection failed for " + ip.field.getName());
				continue;
			}

			ip.field.setAccessible(true);
			try {
				ip.field.set(owner, dependency);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				System.err.println("[DI] Failed to inject singleton of type '"
						+ ip.field.getType().getName() + "' into field '"
						+ ip.field.getName() + "' of class '"
						+ ip.ownerClass.getName() + "': "
						+ e.getMessage());
				e.printStackTrace();
			}
		}

	}

	private Object getSingletonInstance(Class<?> cls) {
		SingletonMeta meta = registry.singletons.get(cls);
		return meta != null ? meta.instance : null;
	}

}
