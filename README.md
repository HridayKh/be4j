# be4j — Backend for Java

**be4j** is a lightweight, annotation-driven backend framework for Java.

It focuses on:
- explicit behavior
- minimal magic
- understanding how frameworks work internally

be4j is built from scratch using core Java concepts like reflection, annotations, and runtime registries.

> Status: early development / experimental / learnign purposes

---

## Features

- Annotation-based dependency injection
- Singleton lifecycle management
- Configuration injection via properties
- HTTP routing with method & path annotations
- Startup-time validation (routes, config, dependencies)

---

## Example

```java
@Singleton
public class AppConfig {
	@Config("app.name")
	public String appName;
}
```

```java
@Path("/users")
public class UserController {

	@Inject
	private final AppConfig config;

	@GET
	@Path("/{id}")
	public User getUser(@PathParam("id") String id) {
		return new User(id);
	}
}
```