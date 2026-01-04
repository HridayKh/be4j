package in.HridayKh.be4j.runtime.di.ReflectionMetas;

public class ParamMeta {
	public final int index; // position in method parameter list
	public final Class<?> type; // String.class, int.class, etc.
	public final String name; // from @PathParam/@QueryParam
	public final ParamSource source; // PATH or QUERY

	public ParamMeta(int index, Class<?> type, String name, ParamSource source) {
		this.index = index;
		this.type = type;
		this.name = name;
		this.source = source;
	}

	public enum ParamSource {
		PATH,
		QUERY,
		HEADER,
		BODY
	}
}
