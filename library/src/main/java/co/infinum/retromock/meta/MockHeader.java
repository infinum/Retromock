package co.infinum.retromock.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import co.infinum.retromock.Retromock;

/**
 * Use this annotation to specify a header in a {@link MockResponse} annotation. This annotation
 * defines a header by name and value. It is applicable only in {@link MockResponse} headers
 * parameter. NOTE: Do not use this annotation directly on service method because
 * {@link Retromock} will ignore it.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MockHeader {

  /**
   * Header name.
   *
   * @return Header name.
   */
  String name();

  /**
   * Header value.
   *
   * @return Header value.
   */
  String value();
}
