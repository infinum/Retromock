package co.infinum.retromock.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import co.infinum.retromock.*;

/**
 * Use this annotation on a service method when you want to specify if this method should be
 * mocked. Presence of this annotation dictates whether {@link Retromock} would mock that call or
 * not. If this method does not exist on a service method {@link Retromock} will redirect a call
 * to delegate instance.
 * Note: Adding this annotation with {@code false} value parameter is equivalent as not adding it.
 * Adding this annotation with {@code true} value parameter is equivalent as adding it without
 * parameters.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mock {

  /**
   * A flag to specify should the service method be mocked or not.
   *
   * @return {@code true} if {@link Retromock} should mock that service method or {@code false}
   * if not.
   */
  boolean value() default true;
}
