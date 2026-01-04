package in.HridayKh.http;

import java.lang.reflect.Method;

public class RouteHandler {

	Object controllerInstance;
	Method method;

	public RouteHandler(Object controllerInstance, Method method) {
		this.controllerInstance = controllerInstance;
		this.method = method;
	}
}
