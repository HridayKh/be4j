package in.HridayKh.sample;

import in.HridayKh.be4j.api.annotations.Body;
import in.HridayKh.be4j.api.annotations.Path;
import in.HridayKh.be4j.api.annotations.PathParam;
import in.HridayKh.be4j.api.annotations.QueryParam;
import in.HridayKh.be4j.api.annotations.Methods.GET;
import in.HridayKh.be4j.api.annotations.Methods.POST;

@Path("/foo")
public class TestTon {

	@Path("/hello")
	@GET
	public String hello() {
		System.out.println("Hello from TestTon singleton!");
		return "Hello from TestTon singleton!";
	}

	@POST
	@Path("/{a}")
	public String users(@PathParam("a") String a, @QueryParam("b") String b, @Body String body) {
		return "a: " + a + " b: " + b + " body: " + body;
	}

}
