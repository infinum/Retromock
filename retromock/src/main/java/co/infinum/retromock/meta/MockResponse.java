package co.infinum.retromock.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;

import co.infinum.retromock.BodyFactory;

import co.infinum.retromock.*;

/**
 * Use this annotation to specify parameters to define {@link MockResponse}.
 * If this method is not applied to a service method {@link Retromock} would create default
 * response with status code 200 and empty response body and no headers.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MockResponses.class)
public @interface MockResponse {

  /**
   * HTTP status code.
   *
   * @return HTTP status code.
   */
  int code() default HttpURLConnection.HTTP_OK;

  /**
   * HTTP status message.
   *
   * @return HTTP status message.
   */
  String message() default "OK";

  /**
   * HTTP response body specifier. This could be either plain response body or a specification
   * for {@link BodyFactory} class. Depending on bodyFactory parameter provided this will be
   * parsed in a different way.
   *
   * @return Body specifier.
   */
  String body() default "";

  /**
   * Array of response headers.
   *
   * @return Array of response headers.
   */
  MockHeader[] headers() default {};

  /**
   * {@link BodyFactory} class used to convert body parameter specifier to
   * {@link java.io.InputStream} body. Note: instance of class provided here has to be registered
   * to {@link Retromock} using {@code addBodyParser} method.
   *
   * @return BodyFactory class used to convert body parameter text to body.
   */
  Class<? extends BodyFactory> bodyFactory() default BodyFactory.class;
}
