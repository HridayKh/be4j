package in.HridayKh.be4j.di.ReflectionMetas;

import java.lang.reflect.Method;

import in.HridayKh.be4j.http.HttpMethod;

public class MethodLevelPathMeta {
	public final Class<?> controllerClass;
	public final Method method;
	public final HttpMethod httpMethod;
	public final String fullPath;

	public MethodLevelPathMeta(Class<?> controllerClass, Method method, HttpMethod httpMethod, String fullPath) {
		this.controllerClass = controllerClass;
		this.method = method;
		this.httpMethod = httpMethod;
		this.fullPath = fullPath;
	}

}
