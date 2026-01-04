package in.HridayKh.be4j.http;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import in.HridayKh.be4j.di.Registry;
import in.HridayKh.be4j.di.ReflectionMetas.MethodLevelPathMeta;

public class RouteRegistry {

	private Registry registry = null;
	private final Map<HttpMethod, Map<String, RouteHandler>> routes = new HashMap<>();

	public RouteRegistry(Registry registry) {
		this.registry = registry;
	}

	public RouteHandler find(HttpMethod method, String path) {
		return routes.getOrDefault(method, Map.of()).get(path);
	}

	private void register(HttpMethod method, String path, Object controllerInstance, Method methodRef) {
		Map<String, RouteHandler> methodRoutes = routes.computeIfAbsent(method, k -> new HashMap<>());

		if (methodRoutes.containsKey(path))
			throw new RuntimeException("Duplicate route at runtime: " + method + " " + path);

		methodRoutes.put(path, new RouteHandler(controllerInstance, methodRef));

		System.out.println("[ROUTE REGISTRY] Registered route: " + method + " " + path +
				" -> " + controllerInstance.getClass().getName());
	}

	public void registerAll() {
		for (MethodLevelPathMeta mlpm : registry.methodLevelPaths) {
			Object controllerInstance = registry.singletons.get(mlpm.controllerClass).instance;
			if (controllerInstance == null)
				throw new RuntimeException(
						"Controller " + mlpm.controllerClass.getName() +
								" is not a managed singleton");
			String baseRoute = registry.classLevelPaths.getOrDefault(mlpm.controllerClass, "");
			String fullPath = (normalizePath(baseRoute) + normalizePath(mlpm.fullPath)).replace("//", "/");
			register(mlpm.httpMethod, fullPath, controllerInstance, mlpm.method);
		}
	}

	private String normalizePath(String path) {
		if (path == null || path.isBlank())
			return "/";

		while (path.contains("//"))
			path = path.replaceAll("//", "/");

		if (!path.startsWith("/"))
			path = "/" + path;

		if (path.endsWith("/"))
			path = path.substring(0, path.length() - 1);

		return path;
	}

}
