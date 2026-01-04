### Added
- `@Body`: Binds the HTTP request body to a handler method parameter. The runtime deserializes the request body into the parameter type using the request `Content-Type` (for example, `application/json`).
- `@Consumes`: Declares accepted request media types at the method or controller level. When present, the runtime validates the request `Content-Type` and selects the appropriate deserializer.
- `@QueryParam`: Binds HTTP query parameters to method parameters by name. Supports primitive types and basic POJO conversion.
- `@PathParam`: Binds path template variables (from `@Path`) to method parameters by name. Performs basic type conversion (e.g., `String` → `int`) where applicable.

### Usage example
```java
@Path("/users/{id}")
@Consumes("application/json")
public Response updateUser(
		@PathParam("id") int id, 
		@QueryParam("notify") boolean notify, 
		@Body UserUpdateRequest body
	) { ... }
```

### Notes
- If no `@Consumes` is provided, the handler attempts to infer the deserializer from the request `Content-Type`.
- Missing or malformed parameter values result in a 400-style error at dispatch time (consistent with existing routing behavior).
- Backwards compatible: existing handlers continue to work without changes. Add these annotations to opt into body deserialization and media-type validation.

### Testing & Validation
- Add integration tests that send requests with matching `Content-Type`, query strings, and path parameter values to verify deserialization and parameter binding.

