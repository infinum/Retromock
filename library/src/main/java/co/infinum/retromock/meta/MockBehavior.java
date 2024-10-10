package co.infinum.retromock.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to specify a network behavior for a mocked call.
 * Behavior supports specifying a call delay with a delay mean {@code durationMillis} and
 * variation {@code durationDeviation}. For example,
 * <pre><code>
 *   durationMillis = 1000
 *   durationDeviation = 500
 * </code></pre>
 * would produce a random delay in range [500, 1500).
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MockBehavior {

  /**
   * Default duration in milliseconds.
   */
  int DEFAULT_DURATION_MILLIS = 1000;
  /**
   * Default deviation in milliseconds.
   */
  int DEFAULT_DEVIATION_MILLIS = 500;

  /**
   * A mean of the delay in milliseconds.
   *
   * @return A mean of the delay in milliseconds.
   */
  int durationMillis() default DEFAULT_DURATION_MILLIS;

  /**
   * A deviation of the delay in milliseconds.
   *
   * @return A deviation of the delay in milliseconds.
   */
  int durationDeviation() default DEFAULT_DEVIATION_MILLIS;
}
