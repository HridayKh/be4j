package in.HridayKh.DI.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Indicates that the annotated type should be treated as a singleton by the
 * dependency injection container.
 *
 * <p>
 * Attributes:
 * <ul>
 * <li><b>order</b> — an integer priority for this singleton. Lower values
 * denote higher priority when the container must choose between multiple
 * candidate singletons. Defaults to 0.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Example:
 * 
 * <pre>{@code
 * &#64;Singleton(order = 5)
 * public class MyService { ... }
 * }</pre>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Singleton {
	/**
	 * Order of the singleton instance, lower number first
	 */
	int order() default Integer.MAX_VALUE;
}