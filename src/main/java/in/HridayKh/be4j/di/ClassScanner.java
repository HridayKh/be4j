package in.HridayKh.be4j.di;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import in.HridayKh.be4j.annotations.Config;
import in.HridayKh.be4j.annotations.Inject;
import in.HridayKh.be4j.annotations.Path;
import in.HridayKh.be4j.annotations.Singleton;
import in.HridayKh.be4j.annotations.Methods.DELETE;
import in.HridayKh.be4j.annotations.Methods.GET;
import in.HridayKh.be4j.annotations.Methods.PATCH;
import in.HridayKh.be4j.annotations.Methods.POST;
import in.HridayKh.be4j.annotations.Methods.PUT;
import in.HridayKh.be4j.config.ConfigLoader;
import in.HridayKh.be4j.di.ReflectionMetas.ConfigInjectionPoint;
import in.HridayKh.be4j.di.ReflectionMetas.InjectPoint;
import in.HridayKh.be4j.di.ReflectionMetas.MethodLevelPathMeta;
import in.HridayKh.be4j.di.ReflectionMetas.SingletonMeta;
import in.HridayKh.be4j.http.HttpMethod;

public class ClassScanner {

	private Registry registry = null;
	private ConfigLoader config = null;

	public ClassScanner(ConfigLoader config, Registry reg) {
		this.config = config;
		this.registry = reg;
	}

	private static final Map<Class<? extends Annotation>, HttpMethod> HTTP_ANNOTATIONS = Map.of(
			GET.class, HttpMethod.GET,
			POST.class, HttpMethod.POST,
			PUT.class, HttpMethod.PUT,
			PATCH.class, HttpMethod.PATCH,
			DELETE.class, HttpMethod.DELETE);

	public void scan() {
		String pkg = config.get("server.app.package", "in.HridayKh.sample");
		System.out.println("[SCAN] Starting classpath scan for package: " + pkg);

		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.forPackages(pkg)
				.filterInputsBy(new FilterBuilder().includePackage(pkg))
				.setScanners(Scanners.SubTypes.filterResultsBy(c -> true)));

		Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);

		for (Class<?> cls : allClasses) {
			System.out.println("[SCAN] Scanning class " + cls.getName());
			// @Singleton
			if (cls.isAnnotationPresent(Singleton.class)) {
				registry.singletons.put(cls,
						new SingletonMeta(cls, cls.getAnnotation(Singleton.class).order()));
				System.out.println("[SCAN] Registered singleton: " + cls.getName());
			}

			// @Path (class-level)
			if (cls.isAnnotationPresent(Path.class)) {
				registry.classLevelPaths.put(cls,cls.getAnnotation(Path.class).value());
				System.out.println("[SCAN] Registered class-level path: " + cls.getName() + " -> "
						+ cls.getAnnotation(Path.class).value());
			}

			for (Method method : cls.getDeclaredMethods()) {
				// @Path (method-level)
				Set<HttpMethod> httpMethods = extractHttpMethods(method);
				if (httpMethods.isEmpty())
					continue;

				String methodPath = method.isAnnotationPresent(Path.class)
						? method.getAnnotation(Path.class).value()
						: "";

				for (HttpMethod httpMethod : httpMethods) {
					registry.methodLevelPaths.add(
							new MethodLevelPathMeta(cls, method, httpMethod, methodPath));
					System.out.println("[SCAN] Registered method-level path: " + cls.getName() + "#"
							+ method.getName()
							+ " [" + httpMethod + "] " + methodPath);
				}

			}

			for (Field field : cls.getDeclaredFields()) {
				// @Config
				if (field.isAnnotationPresent(Config.class)) {

					try {
						Config cfg = field.getAnnotation(Config.class);

						registry.configInjectionPoints.add(
								new ConfigInjectionPoint(cls, field,
										cfg.configValue(), cfg.defaultValue()));
						System.out.println("[SCAN] Registered config injection point: "
								+ cls.getName()
								+ "#"
								+ field.getName() + " -> " + cfg.configValue());
					} catch (IllegalArgumentException e) {
						System.err.println(
								"[SCAN] Failed to process @Config on field "
										+ field.getName()
										+ " in " + cls.getName());
						e.printStackTrace();
					}
				}

				// @Inject
				if (field.isAnnotationPresent(Inject.class)) {
					registry.injectPoints.add(new InjectPoint(cls, field));
					System.out.println("[SCAN] Registered inject point: " + cls.getName() + "#"
							+ field.getName());
				}
			}
		}
		System.out.println("[SCAN] Completed classpath scan; found " + allClasses.size() + " classes.");
	}

	private Set<HttpMethod> extractHttpMethods(Method method) {
		Set<HttpMethod> methods = EnumSet.noneOf(HttpMethod.class);

		for (var entry : HTTP_ANNOTATIONS.entrySet()) {
			if (method.isAnnotationPresent(entry.getKey())) {
				methods.add(entry.getValue());
			}
		}

		return methods;
	}

}
