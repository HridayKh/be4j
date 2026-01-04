package in.HridayKh.be4j.runtime.di.ReflectionMetas;

import java.lang.reflect.Method;

import in.HridayKh.be4j.api.http.HttpMethods;

public class MethodLevelPathMeta {
	public final Class<?> controllerClass;
	public final Method method;
	public final HttpMethods httpMethod;
	public final String fullPath;

	public MethodLevelPathMeta(Class<?> controllerClass, Method method, HttpMethods httpMethod, String fullPath) {
		this.controllerClass = controllerClass;
		this.method = method;
		this.httpMethod = httpMethod;
		this.fullPath = fullPath;
	}

}
