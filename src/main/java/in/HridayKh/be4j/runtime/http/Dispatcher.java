package in.HridayKh.be4j.runtime.http;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import in.HridayKh.be4j.api.http.HttpMethods;
import in.HridayKh.be4j.runtime.di.ReflectionMetas.ParamMeta;
import in.HridayKh.be4j.runtime.http.RoutesRegistry.RouteHandler;
import tools.jackson.databind.ObjectMapper;

public class Dispatcher implements HttpHandler {

	private final RoutesRegistry routeRegistry;

	public Dispatcher(RoutesRegistry routeRegistry) {
		this.routeRegistry = routeRegistry;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		String path = exchange.getRequestURI().getPath();
		while (path.contains("//"))
			path = path.replace("//", "/");
		path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
		if (path.isEmpty())
			path = "/";
		if (!path.startsWith("/"))
			path = "/" + path;

		HttpMethods httpMethod;
		try {
			httpMethod = HttpMethods.valueOf(exchange.getRequestMethod());
		} catch (IllegalArgumentException e) {
			send(exchange, 405, "Method Not Allowed");
			return;
		}

		RouteHandler handler = routeRegistry.find(httpMethod, path);
		if (handler == null) {
			send(exchange, 404, "Not Found");
			return;
		}

		Map<String, String> queryParams = parseQuery(exchange.getRequestURI().getQuery());
		String rawPAth = routeRegistry.findPathFromHandler(handler, httpMethod);
		Map<String, String> pathParams = findPathParams(rawPAth, path);

		Object[] args = new Object[handler.parameters.size()];
		for (ParamMeta pm : handler.parameters) {
			String rawValue = null;
			switch (pm.source) {
				case PATH -> {
					rawValue = pathParams.get(pm.name);
					args[pm.index] = rawValue == null ? getDefaultForType(pm.type)
							: convertType(rawValue, pm.type);
				}
				case QUERY -> {
					rawValue = queryParams.get(pm.name);
					args[pm.index] = rawValue == null ? getDefaultForType(pm.type)
							: convertType(rawValue, pm.type);
				}
				case BODY -> {
					byte[] bodyBytes = exchange.getRequestBody().readAllBytes();

					String contentType = handler.consumes != null ? handler.consumes
							: exchange.getRequestHeaders().getFirst("Content-Type");

					if (bodyBytes.length == 0) {
						args[pm.index] = getDefaultForType(pm.type);
					} else if (pm.type == String.class
							&& (contentType == null || contentType.contains("text"))) {
						args[pm.index] = new String(bodyBytes, StandardCharsets.UTF_8);
					} else if (pm.type == byte[].class) {
						args[pm.index] = bodyBytes;
					} else if (contentType != null && contentType.contains("json")) {
						ObjectMapper objectMapper = new ObjectMapper();
						args[pm.index] = objectMapper.readValue(bodyBytes, pm.type);
					} else {
						args[pm.index] = getDefaultForType(pm.type); // fallback
					}
				}

				default -> throw new UnsupportedOperationException("Unimplemented case: " + pm.source);
			}
		}
		try {

			Object result = handler.method.invoke(handler.controllerInstance, args);

			if (result == null) {
				send(exchange, 204, "");
			} else {
				send(exchange, 200, result.toString());
			}

		} catch (InvocationTargetException | IllegalAccessException e) {
			send(exchange, 500, "Internal Server Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private Map<String, String> findPathParams(String rawPath, String path) {
		Map<String, String> pathParams = new HashMap<>();

		String[] rawSegments = rawPath.split("/");
		String[] actualSegments = path.split("/");
		if (actualSegments.length != rawSegments.length)
			return Map.of();
		for (int i = 0; i < rawSegments.length; i++) {
			String rawSeg = rawSegments[i];
			if (rawSeg.startsWith("{") && rawSeg.endsWith("}")) {
				String paramName = rawSeg.substring(1, rawSeg.length() - 1);
				pathParams.put(paramName, actualSegments[i]);
			}
		}

		return pathParams;
	}

	private void send(HttpExchange exchange, int status, String body) throws IOException {
		byte[] bytes = (body + "\n").getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(status, bytes.length);
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
		}
	}

	private Map<String, String> parseQuery(String query) {
		Map<String, String> params = new HashMap<>();
		if (query == null || query.isEmpty())
			return params;

		for (String pair : query.split("&")) {
			String[] kv = pair.split("=", 2);
			String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
			String value = kv.length > 1
					? URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
					: "";
			params.put(key, value);
		}
		return params;
	}

	private Object convertType(String value, Class<?> type) {
		if (type == String.class)
			return value;
		if (type == int.class || type == Integer.class)
			return Integer.parseInt(value);
		if (type == boolean.class || type == Boolean.class)
			return Boolean.parseBoolean(value);
		if (type == long.class || type == Long.class)
			return Long.parseLong(value);
		if (type == double.class || type == Double.class)
			return Double.parseDouble(value);
		if (type == float.class || type == Float.class)
			return Float.parseFloat(value);
		if (type == short.class || type == Short.class)
			return Short.parseShort(value);
		if (type == byte.class || type == Byte.class)
			return Byte.parseByte(value);
		if (type == char.class || type == Character.class)
			return (value != null && !value.isEmpty()) ? value.charAt(0) : '\u0000';

		return value;
	}

	private Object getDefaultForType(Class<?> type) {
		if (type.isPrimitive()) {
			if (type == boolean.class)
				return false;
			if (type == int.class)
				return 0;
			if (type == long.class)
				return 0L;
			if (type == double.class)
				return 0.0;
			if (type == float.class)
				return 0.0f;
			if (type == char.class)
				return '\u0000';
			if (type == byte.class)
				return (byte) 0;
			if (type == short.class)
				return (short) 0;
			if (type == void.class)
				return null;
		}
		if (type == String.class) {
			return "";
		}
		return null;
	}

}
