package in.HridayKh.be4j.runtime.di.ReflectionMetas;

import java.lang.reflect.Method;
import java.util.List;

import in.HridayKh.be4j.api.http.HttpMethods;

public class MethodLevelPathMeta {
	public final Class<?> controllerClass;
	public final Method method;
	public final HttpMethods httpMethod;
	public final String fullPath;
	public final String consumes;

	public final List<ParamMeta> parameters;

	public MethodLevelPathMeta(Class<?> controllerClass, Method method,
			HttpMethods httpMethod, String fullPath, List<ParamMeta> parameters, String consumes) {
		this.controllerClass = controllerClass;
		this.method = method;
		this.httpMethod = httpMethod;
		this.fullPath = fullPath;
		this.parameters = parameters;
		this.consumes = consumes;
	}

}
