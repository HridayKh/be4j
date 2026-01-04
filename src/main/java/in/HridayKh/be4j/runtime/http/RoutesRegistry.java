package in.HridayKh.be4j.runtime.http;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.HridayKh.be4j.api.http.HttpMethods;
import in.HridayKh.be4j.runtime.di.Registry;
import in.HridayKh.be4j.runtime.di.ReflectionMetas.MethodLevelPathMeta;
import in.HridayKh.be4j.runtime.di.ReflectionMetas.ParamMeta;

public class RoutesRegistry {

	private Registry registry = null;
	private final Map<HttpMethods, Map<String, RouteHandler>> routes = new HashMap<>();

	public RoutesRegistry(Registry registry) {
		this.registry = registry;
	}

	public RouteHandler find(HttpMethods method, String path) {
		Map<String, RouteHandler> methodRoutes = routes.get(method);
		if (methodRoutes == null)
			return null;

		// match literal path first
		RouteHandler handler = methodRoutes.get(path);
		if (handler != null)
			return handler;

		// match parameterized routes
		for (Map.Entry<String, RouteHandler> entry : methodRoutes.entrySet()) {
			String routePattern = entry.getKey();
			if (routePattern.contains("{")) {
				Map<String, String> params = matchRoute(routePattern, path);
				if (params != null) 
					return entry.getValue();
			}
		}
		return null;
	}

	private Map<String, String> matchRoute(String routePattern, String actualPath) {
		// Convert route pattern to regex
		String regex = routePattern.replaceAll("\\{([^}]+)}", "([^/]+)");
		Pattern pattern = Pattern.compile("^" + regex + "$");
		Matcher matcher = pattern.matcher(actualPath);

		if (!matcher.matches()) {
			return null;
		}

		Map<String, String> params = new HashMap<>();
		Pattern paramPattern = Pattern.compile("\\{([^}]+)}");
		Matcher paramMatcher = paramPattern.matcher(routePattern);

		int groupIndex = 1;
		while (paramMatcher.find()) {
			String paramName = paramMatcher.group(1);
			String paramValue = matcher.group(groupIndex++);
			params.put(paramName, paramValue);
		}

		return params;
	}

	private void register(HttpMethods method, String path, Object controllerInstance, Method methodRef, List<ParamMeta> parameters, String consumes) {
		Map<String, RouteHandler> methodRoutes = routes.computeIfAbsent(method, k -> new HashMap<>());

		if (methodRoutes.containsKey(path))
			throw new RuntimeException("Duplicate route at runtime: " + method + " " + path);

		methodRoutes.put(path, new RouteHandler(controllerInstance, methodRef, parameters, consumes));

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
			register(mlpm.httpMethod, fullPath, controllerInstance, mlpm.method, mlpm.parameters, mlpm.consumes);
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

	public class RouteHandler {

		Object controllerInstance;
		Method method;
		List<ParamMeta> parameters;
		String consumes;

		public RouteHandler(Object controllerInstance, Method method, List<ParamMeta> parameters, String consumes) {
			this.controllerInstance = controllerInstance;
			this.method = method;
			this.parameters = parameters;
			this.consumes = consumes;
		}
	}

	public String findPathFromHandler(RouteHandler handler, HttpMethods httpMethod) {
		Map<String, RouteHandler> methodRoutes = routes.get(httpMethod);
		if (methodRoutes != null) {
			for (Map.Entry<String, RouteHandler> entry : methodRoutes.entrySet()) {
				if (entry.getValue().equals(handler)) {
					return entry.getKey();
				}
			}
		}
		return null;
	}

}
