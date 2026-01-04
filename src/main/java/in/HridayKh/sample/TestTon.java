package in.HridayKh.sample;

import in.HridayKh.DI.annotations.Path;
import in.HridayKh.DI.annotations.Methods.GET;

@Path("/foo")
public class TestTon {

	@Path("/hello")
	@GET
	public String hello() {
		System.out.println("Hello from TestTon singleton!");
		return "Hello from TestTon singleton!";
	}
}
