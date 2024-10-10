package co.infinum.retromock.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to mark the method that provides a mock
 * {@link co.infinum.retromock.Response}.
 *
 * Create a class that has a method with equal parameters to a
 * service method. Return type should be of type {@link co.infinum.retromock.Response}.
 * That method should be annotated with this annotation for
 * {@link co.infinum.retromock.Retromock} to find it.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProvidesMock {
}
