package in.HridayKh.be4j.runtime.http;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import in.HridayKh.be4j.api.http.HttpMethods;
import in.HridayKh.be4j.runtime.http.RoutesRegistry.RouteHandler;

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

		String methodName = exchange.getRequestMethod();

		HttpMethods httpMethod;
		try {
			httpMethod = HttpMethods.valueOf(methodName);
		} catch (IllegalArgumentException e) {
			send(exchange, 405, "Method Not Allowed");
			return;
		}

		RouteHandler handler = routeRegistry.find(httpMethod, path);

		if (handler == null) {
			send(exchange, 404, "Not Found");
			return;
		}

		try {
			Object result = handler.method.invoke(handler.controllerInstance);

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

	private void send(HttpExchange exchange, int status, String body) throws IOException {
		byte[] bytes = (body + "\n").getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(status, bytes.length);
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
		}
	}
}
