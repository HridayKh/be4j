package in.HridayKh;

import in.HridayKh.be4j.runtime.config.ConfigLoader;
import in.HridayKh.be4j.runtime.di.ClassScanner;
import in.HridayKh.be4j.runtime.di.DiContainer;
import in.HridayKh.be4j.runtime.di.Registry;
import in.HridayKh.be4j.runtime.di.RegistryValidator;
import in.HridayKh.be4j.runtime.http.Http;
import in.HridayKh.be4j.runtime.http.RoutesRegistry;

public class Main {

	public static void main(String[] args) {
		System.out.println("[CONFIG] Loading configuration...");
		long startTime = System.currentTimeMillis();

		ConfigLoader config = new ConfigLoader("application.properties");
		config.load();

		long preScanTime = System.currentTimeMillis();
		System.out.println("[CONFIG] Configuration loaded in " + (preScanTime - startTime) + " ms");
		System.out.println("\n[SCAN] Starting classpath scan and registry population...");

		Registry registry = new Registry();
		ClassScanner classScanner = new ClassScanner(config, registry);
		classScanner.scan();

		long preValidationTime = System.currentTimeMillis();
		System.out.println("[SCAN] Classpath scan completed in " + (preValidationTime - preScanTime) + " ms");
		System.out.println("\n[VALIDATION] Starting registry validation...");

		RegistryValidator registryValidator = new RegistryValidator(config, registry);
		registryValidator.validate();

		long preDiContainer = System.currentTimeMillis();
		System.out.println("[VALIDATION] Registry validation completed in " + (preDiContainer - preValidationTime) + " ms");
		System.out.println("\n[DI] Starting dependency injection...");

		DiContainer diContainer = new DiContainer(config, registry);
		diContainer.injectSingletons();
		diContainer.injectConfigs();

		long preRouteRegistrationTime = System.currentTimeMillis();
		System.out.println("[DI] Dependency injection completed in " + (preRouteRegistrationTime - preDiContainer) + " ms");
		System.out.println("\n[ROUTE REGISTRY] Starting route registration...");

		RoutesRegistry routeRegistry = new RoutesRegistry(registry);
		routeRegistry.registerAll();

		long endTime = System.currentTimeMillis();
		System.out.println("[ROUTE REGISTRY] Route registration completed in " + (endTime - preRouteRegistrationTime) + " ms");
		System.out.println("\n[SETUP] Total setup time: " + (endTime - startTime) + " ms\n");

		Http http = new Http(config);
		http.createServer(routeRegistry);
		// http.killServer();
	}

	

}
