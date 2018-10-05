package co.infinum.retromock.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies repeatable annotation for {@link MockResponse} type.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MockResponses {

  /**
   * Array of {@link MockResponse} annotations that are applied to the service method.
   *
   * @return MockResponse annotations applied to the service method.
   */
  MockResponse[] value() default {};
}
