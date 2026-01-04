package in.HridayKh.sample;

import in.HridayKh.be4j.api.annotations.Path;
import in.HridayKh.be4j.api.annotations.Methods.GET;

@Path("/foo")
public class TestTon {

	@Path("/hello")
	@GET
	public String hello() {
		System.out.println("Hello from TestTon singleton!");
		return "Hello from TestTon singleton!";
	}
}
