# Annotations:

## Config
- @Config("a.b")

## DI
- @Singleton(100) # optional weight/order of initialization
- @Inject / @UseSingleton

## HTTP

### Routing
- @Path("/design/*")
- @GET/POST/PUT/PATCH/DELETE

# Middleware
- @Middleware/Filter("/path/", 100) # weight/order of execution
- @ExceptionHandler("/path/", 404) # on class that manages a specific error code

## Parameters
- @PathParam("paramName")
- @QueryParam("paramName")
- @HeaderParam("headerName")
- @Body
- @Consumes("media/type")
- @Produces("media/type")
