package in.HridayKh.be4j.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import in.HridayKh.be4j.config.ConfigLoader;

public class Http {

	private static HttpServer server = null;

	private ConfigLoader config = null;

	public Http(ConfigLoader config) {
		this.config = config;
	}

	public void createServer(RouteRegistry routeRegistry) {
		long startTime = System.currentTimeMillis();

		int port = config.getInt("server.port", 8080);
		
		System.out.println("[HTTP] HTTP Server Starting on port " + port + "...");
		try {
			server = HttpServer.create();
			server.bind(new InetSocketAddress(port), 0);
		} catch (IOException e) {
			e.printStackTrace();
			long endTime = System.currentTimeMillis();
			System.out.println("[HTTP] HTTP Server Start Failed in " + (endTime - startTime) + " ms on port " + port);
			return;
		}

		server.createContext("/", new HandleRootContext(routeRegistry));
		server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
		server.start();

		long endTime = System.currentTimeMillis();
		System.out.println("[HTTP] HTTP Server Started in " + (endTime - startTime) + " ms on port " + port);
	}

	public void killServer() {
		System.out.println("[HTTP] Killing HTTP Server...");
		long startTime = System.currentTimeMillis();
		if (server != null) {
			server.stop(0);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("[HTTP] HTTP Server Killed in " + (endTime - startTime) + " ms");
	}
}