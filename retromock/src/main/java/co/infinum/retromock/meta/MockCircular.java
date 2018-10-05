package co.infinum.retromock.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import co.infinum.retromock.*;

/**
 * Use this to specify resolution strategy when applying multiple responses. If service method
 * has multiple {@link MockResponse} annotations and if service method has this annotation
 * provided {@link Retromock} would iterate through responses in a circular way. For example,
 * for three responses specified (r1, r2, and r3) and calling this method five times it would
 * produce following response output: r1, r2, r3, r1, r2, ...
 * Note: this annotation is cannot coexist with {@link MockRandom} or {@link MockSequential}
 * because they specify different behavior for the same purpose.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MockCircular {
}
