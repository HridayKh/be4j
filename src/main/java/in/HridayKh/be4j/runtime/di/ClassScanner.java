package in.HridayKh.be4j.runtime.di;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import in.HridayKh.be4j.api.annotations.Body;
import in.HridayKh.be4j.api.annotations.Config;
import in.HridayKh.be4j.api.annotations.Consumes;
import in.HridayKh.be4j.api.annotations.Inject;
import in.HridayKh.be4j.api.annotations.Path;
import in.HridayKh.be4j.api.annotations.PathParam;
import in.HridayKh.be4j.api.annotations.QueryParam;
import in.HridayKh.be4j.api.annotations.Singleton;
import in.HridayKh.be4j.api.annotations.Methods.DELETE;
import in.HridayKh.be4j.api.annotations.Methods.GET;
import in.HridayKh.be4j.api.annotations.Methods.PATCH;
import in.HridayKh.be4j.api.annotations.Methods.POST;
import in.HridayKh.be4j.api.annotations.Methods.PUT;
import in.HridayKh.be4j.api.http.HttpMethods;
import in.HridayKh.be4j.runtime.config.ConfigLoader;
import in.HridayKh.be4j.runtime.di.ReflectionMetas.ConfigInjectionPoint;
import in.HridayKh.be4j.runtime.di.ReflectionMetas.InjectPoint;
import in.HridayKh.be4j.runtime.di.ReflectionMetas.MethodLevelPathMeta;
import in.HridayKh.be4j.runtime.di.ReflectionMetas.ParamMeta;
import in.HridayKh.be4j.runtime.di.ReflectionMetas.ParamMeta.ParamSource;
import in.HridayKh.be4j.runtime.di.ReflectionMetas.SingletonMeta;

public class ClassScanner {

	private Registry registry = null;
	private ConfigLoader config = null;

	public ClassScanner(ConfigLoader config, Registry reg) {
		this.config = config;
		this.registry = reg;
	}

	private static final Map<Class<? extends Annotation>, HttpMethods> HTTP_ANNOTATIONS = Map.of(
			GET.class, HttpMethods.GET,
			POST.class, HttpMethods.POST,
			PUT.class, HttpMethods.PUT,
			PATCH.class, HttpMethods.PATCH,
			DELETE.class, HttpMethods.DELETE);

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
				registry.classLevelPaths.put(cls, cls.getAnnotation(Path.class).value());
				System.out.println("[SCAN] Registered class-level path: " + cls.getName() + " -> "
						+ cls.getAnnotation(Path.class).value());
			}

			for (Method method : cls.getDeclaredMethods()) {
				// @Path (method-level)
				Set<HttpMethods> httpMethods = extractHttpMethods(method);
				if (httpMethods.isEmpty())
					continue;

				String methodPath = method.isAnnotationPresent(Path.class)
						? method.getAnnotation(Path.class).value()
						: "";

				String consumes = null;
				if (method.isAnnotationPresent(Consumes.class))
					consumes = method.getAnnotation(Consumes.class).value();

				Parameter[] methodParams = method.getParameters();
				List<ParamMeta> parameters = new ArrayList<>();
				for (int i = 0; i < methodParams.length; i++) {
					Parameter param = methodParams[i];
					if (param.isAnnotationPresent(PathParam.class)) {
						PathParam pp = param.getAnnotation(PathParam.class);
						parameters.add(new ParamMeta(i, param.getType(), pp.value(),
								ParamSource.PATH));
					} else if (param.isAnnotationPresent(QueryParam.class)) {
						QueryParam qp = param.getAnnotation(QueryParam.class);
						parameters.add(new ParamMeta(i, param.getType(), qp.value(),
								ParamSource.QUERY));
					} else if (param.isAnnotationPresent(Body.class)) {
						parameters.add(new ParamMeta(i, param.getType(), null,
								ParamSource.BODY));
					}
					// @HeaderParam to be added here later
				}

				for (HttpMethods httpMethod : httpMethods) {
					MethodLevelPathMeta mlpm = new MethodLevelPathMeta(cls, method, httpMethod,
							methodPath, parameters, consumes);
					registry.methodLevelPaths.add(mlpm);
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

	private Set<HttpMethods> extractHttpMethods(Method method) {
		Set<HttpMethods> methods = EnumSet.noneOf(HttpMethods.class);

		for (var entry : HTTP_ANNOTATIONS.entrySet()) {
			if (method.isAnnotationPresent(entry.getKey())) {
				methods.add(entry.getValue());
			}
		}

		return methods;
	}

}
